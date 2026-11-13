package com.openwhisper.android.editor

/**
 * Many apps (WhatsApp, Messages, …) report an empty field's placeholder, such as
 * "Mensagem", as the node's text. Treating it as real content made dictation
 * produce "Mensagem <transcript>". This decides what the field really contains.
 */
object EditorHintPolicy {
    data class FieldText(val text: String, val selectionStart: Int, val selectionEnd: Int)

    fun resolve(
        reportedText: String,
        hintText: String?,
        isShowingHintText: Boolean,
        selectionStart: Int,
        selectionEnd: Int,
    ): FieldText {
        if (isShowingHintText) return FieldText("", 0, 0)
        val hint = hintText?.takeIf(String::isNotEmpty)
        // Fallback for apps that don't flag the hint: the reported text is exactly
        // the hint and there is no cursor inside it. Text the user really typed
        // always has a valid cursor, so it is never discarded by this rule.
        val hasCursorInText = selectionStart in 0..reportedText.length &&
            selectionEnd in 0..reportedText.length
        if (hint != null && reportedText == hint && !hasCursorInText) {
            return FieldText("", 0, 0)
        }
        return FieldText(reportedText, selectionStart, selectionEnd)
    }
}
