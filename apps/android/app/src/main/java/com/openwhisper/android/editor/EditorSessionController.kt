package com.openwhisper.android.editor

interface EditableNode {
    val stableId: String
    fun snapshot(): EditorSnapshot
    fun setText(text: String): Boolean
    fun setSelection(cursor: Int): Boolean
}

fun interface FocusedEditorProvider {
    fun focusedEditor(): EditableNode?
}

data class EditorSession(
    val stableId: String,
    val snapshot: EditorSnapshot? = null,
    val channel: EditorSessionChannel = EditorSessionChannel.Accessibility,
)

enum class EditorSessionChannel {
    Accessibility,
    InputConnection,
}

enum class EditorCommitResult {
    Committed,
    FocusChanged,
    ContentChanged,
    Unsupported,
}

interface EditorSessionPort {
    fun begin(): EditorSession?
    fun commit(session: EditorSession, transcript: String): EditorCommitResult
}

class EditorSessionController(
    private val provider: FocusedEditorProvider,
    private val planner: EditorInsertionPlanner = EditorInsertionPlanner(),
) : EditorSessionPort {
    override fun begin(): EditorSession? {
        val editor = provider.focusedEditor() ?: return null
        val snapshot = editor.snapshot()
        if (snapshot.isPassword || snapshot.isSensitive) return null
        return EditorSession(editor.stableId, snapshot)
    }

    override fun commit(session: EditorSession, transcript: String): EditorCommitResult {
        val editor = provider.focusedEditor() ?: return EditorCommitResult.FocusChanged
        if (editor.stableId != session.stableId) return EditorCommitResult.FocusChanged
        val captured = session.snapshot ?: return EditorCommitResult.Unsupported
        if (editor.snapshot() != captured) return EditorCommitResult.ContentChanged

        return when (val plan = planner.plan(captured, transcript)) {
            is InsertionPlan.SetText -> {
                if (!editor.setText(plan.text)) return EditorCommitResult.Unsupported
                editor.setSelection(plan.cursor)
                EditorCommitResult.Committed
            }
            is InsertionPlan.NoOp -> EditorCommitResult.Committed
            is InsertionPlan.Rejected -> EditorCommitResult.Unsupported
        }
    }
}

/**
 * Prefers the keyboard's InputConnection, which reports the field's real text and
 * inserts at the real cursor. Accessibility nodes of many apps (WhatsApp, Messages)
 * report an empty field's placeholder ("Mensagem") as its text, which leaked into
 * the dictated result. Accessibility remains the fallback, and its password /
 * sensitive flags still veto an InputConnection session ([isProtected]).
 */
class RoutingEditorSessionPort(
    private val inputConnection: EditorSessionPort,
    private val accessibility: EditorSessionPort,
    private val isProtected: () -> Boolean = { false },
) : EditorSessionPort {
    override fun begin(): EditorSession? {
        val session = inputConnection.begin() ?: return accessibility.begin()
        return if (isProtected()) null else session
    }

    override fun commit(session: EditorSession, transcript: String): EditorCommitResult =
        when (session.channel) {
            EditorSessionChannel.InputConnection -> inputConnection.commit(session, transcript)
            EditorSessionChannel.Accessibility -> accessibility.commit(session, transcript)
        }
}
