package com.openwhisper.android.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorHintPolicyTest {
    private val empty = EditorHintPolicy.FieldText("", 0, 0)

    @Test
    fun flaggedPlaceholderIsTreatedAsAnEmptyField() {
        assertEquals(
            empty,
            EditorHintPolicy.resolve("Mensagem", "Mensagem", isShowingHintText = true, -1, -1),
        )
    }

    @Test
    fun unflaggedPlaceholderWithoutCursorIsTreatedAsEmpty() {
        assertEquals(
            empty,
            EditorHintPolicy.resolve("Mensagem", "Mensagem", isShowingHintText = false, -1, -1),
        )
    }

    @Test
    fun realTextEqualToTheHintIsKeptWhenItHasACursor() {
        assertEquals(
            EditorHintPolicy.FieldText("Mensagem", 8, 8),
            EditorHintPolicy.resolve("Mensagem", "Mensagem", isShowingHintText = false, 8, 8),
        )
    }

    @Test
    fun ordinaryTextIsUntouched() {
        assertEquals(
            EditorHintPolicy.FieldText("Oi tudo bem", 2, 2),
            EditorHintPolicy.resolve("Oi tudo bem", "Mensagem", isShowingHintText = false, 2, 2),
        )
    }

    @Test
    fun placeholderNoLongerLeaksIntoTheInsertedText() {
        val field = EditorHintPolicy.resolve("Mensagem", "Mensagem", true, -1, -1)
        val plan = EditorInsertionPlanner().plan(
            EditorSnapshot(field.text, field.selectionStart, field.selectionEnd),
            "Fazer o deploy no GitHub",
        )
        assertEquals(InsertionPlan.SetText("Fazer o deploy no GitHub", 24), plan)
    }
}
