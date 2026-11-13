package com.openwhisper.android.transcription

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VocabularyTest {
    @Test
    fun parsesLinesAndCommasTrimsAndDeduplicatesCaseInsensitively() {
        assertEquals(
            listOf("OpenWhisper", "Kubernetes", "PIX", "São Paulo"),
            Vocabulary.parse(" OpenWhisper \n\nKubernetes, PIX\r\nopenwhisper\n  São   Paulo "),
        )
    }

    @Test
    fun stripsCharactersTheApiRejects() {
        assertEquals(listOf("a b"), Vocabulary.parse("<a>b"))
    }

    @Test
    fun capsTermLengthAndCount() {
        val terms = Vocabulary.parse((1..150).joinToString("\n") { "term$it" + "x".repeat(80) })
        assertEquals(Vocabulary.MAX_TERMS, terms.size)
        assertTrue(terms.all { it.length <= Vocabulary.MAX_TERM_LENGTH })
    }

    @Test
    fun keywordModelsKeepTheDictionaryOutOfThePrompt() {
        val prompt = Vocabulary.prompt(
            TranscriptionModels.GPT_TRANSCRIBE,
            TranscriptionLanguage.AUTOMATIC,
            listOf("OpenWhisper"),
            continuation = "previous text",
        )
        assertEquals("previous text", prompt)
    }

    @Test
    fun promptModelsGetTheDictionaryAheadOfTheContinuation() {
        val prompt = Vocabulary.prompt(
            TranscriptionModels.GPT_4O_MINI_TRANSCRIBE,
            TranscriptionLanguage.AUTOMATIC,
            listOf("OpenWhisper", "PIX"),
            continuation = "previous text",
        )
        assertEquals("Vocabulary: OpenWhisper, PIX. previous text", prompt)
    }

    @Test
    fun whisperPromptStaysWithinItsLimit() {
        val terms = (1..100).map { "Term$it" }
        val prompt = Vocabulary.prompt(
            TranscriptionModels.WHISPER_1,
            TranscriptionLanguage.AUTOMATIC,
            terms,
            continuation = "x".repeat(2_000),
        )
        assertTrue(prompt.length <= TranscriptionModels.WHISPER_1.maxPromptChars)
        assertTrue(prompt.startsWith("Vocabulary: Term1, Term2"))
        assertFalse(prompt.contains("Term100"))
    }

    @Test
    fun unknownStoredModelFallsBackToTheDefault() {
        assertEquals(TranscriptionModels.DEFAULT, TranscriptionModels.byId("removed-model"))
        assertEquals(TranscriptionModels.WHISPER_1, TranscriptionModels.byId("whisper-1"))
    }

    @Test
    fun brazilianPortuguesePromptIsWrittenInPortugueseAndKeepsEnglishTerms() {
        val prompt = Vocabulary.prompt(
            TranscriptionModels.GPT_4O_MINI_TRANSCRIBE,
            TranscriptionLanguage.PORTUGUESE_BRAZIL,
            listOf("Git", "GitHub", "deploy", "MCP"),
            continuation = "",
        )
        assertEquals(
            TranscriptionLanguage.PORTUGUESE_BRAZIL.instruction +
                " Vocabulário: Git, GitHub, deploy, MCP.",
            prompt,
        )
    }

    @Test
    fun keywordModelStillGetsTheLanguageInstruction() {
        val prompt = Vocabulary.prompt(
            TranscriptionModels.GPT_TRANSCRIBE,
            TranscriptionLanguage.PORTUGUESE_BRAZIL,
            listOf("Git"),
            continuation = "frase anterior",
        )
        assertEquals(TranscriptionLanguage.PORTUGUESE_BRAZIL.instruction + " frase anterior", prompt)
    }

    @Test
    fun whisperPortuguesePromptStaysWithinItsLimit() {
        val prompt = Vocabulary.prompt(
            TranscriptionModels.WHISPER_1,
            TranscriptionLanguage.PORTUGUESE_BRAZIL,
            (1..100).map { "Termo$it" },
            continuation = "x".repeat(2_000),
        )
        assertTrue(prompt.length <= TranscriptionModels.WHISPER_1.maxPromptChars)
        assertTrue(prompt.startsWith(TranscriptionLanguage.PORTUGUESE_BRAZIL.instruction))
        assertTrue(prompt.contains("Vocabulário: Termo1, Termo2"))
    }

    @Test
    fun defaultsToBrazilianPortugueseAndMapsToTheIsoCode() {
        assertEquals(TranscriptionLanguage.PORTUGUESE_BRAZIL, TranscriptionLanguage.byId(null))
        assertEquals("pt", TranscriptionLanguage.PORTUGUESE_BRAZIL.apiCode)
        assertEquals(null, TranscriptionLanguage.AUTOMATIC.apiCode)
    }
}
