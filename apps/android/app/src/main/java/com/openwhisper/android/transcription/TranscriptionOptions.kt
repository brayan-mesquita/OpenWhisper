package com.openwhisper.android.transcription

import com.openwhisper.android.R

/**
 * An OpenAI speech-to-text model the user can pick in settings.
 *
 * Prices shown in the UI are OpenAI's published per-minute list prices at the
 * time of writing and are only a guide; OpenAI's pricing page is authoritative.
 */
data class TranscriptionModel(
    val id: String,
    val labelRes: Int,
    val descriptionRes: Int,
    /** Model accepts the dedicated `keywords[]` vocabulary field. */
    val supportsKeywords: Boolean,
    /** Conservative upper bound for the `prompt` field, in characters. */
    val maxPromptChars: Int,
)

object TranscriptionModels {
    val GPT_TRANSCRIBE = TranscriptionModel(
        id = "gpt-transcribe",
        labelRes = R.string.model_gpt_transcribe,
        descriptionRes = R.string.model_gpt_transcribe_description,
        supportsKeywords = true,
        maxPromptChars = 4_000,
    )
    val GPT_4O_MINI_TRANSCRIBE = TranscriptionModel(
        id = "gpt-4o-mini-transcribe",
        labelRes = R.string.model_gpt_4o_mini_transcribe,
        descriptionRes = R.string.model_gpt_4o_mini_transcribe_description,
        supportsKeywords = false,
        maxPromptChars = 4_000,
    )
    val GPT_4O_TRANSCRIBE = TranscriptionModel(
        id = "gpt-4o-transcribe",
        labelRes = R.string.model_gpt_4o_transcribe,
        descriptionRes = R.string.model_gpt_4o_transcribe_description,
        supportsKeywords = false,
        maxPromptChars = 4_000,
    )
    val WHISPER_1 = TranscriptionModel(
        id = "whisper-1",
        labelRes = R.string.model_whisper_1,
        descriptionRes = R.string.model_whisper_1_description,
        supportsKeywords = false,
        // whisper-1 prompts are limited to 224 tokens; stay well below it.
        maxPromptChars = 400,
    )

    val ALL = listOf(GPT_TRANSCRIBE, GPT_4O_MINI_TRANSCRIBE, GPT_4O_TRANSCRIBE, WHISPER_1)
    val DEFAULT = GPT_TRANSCRIBE

    fun byId(id: String?): TranscriptionModel = ALL.firstOrNull { it.id == id } ?: DEFAULT
}

/**
 * Spoken language. The API takes an ISO-639-1 code; there is no regional code,
 * so Brazilian Portuguese is `pt` plus a Brazilian-Portuguese prompt that steers
 * spelling and tells the model to keep English technical terms in English.
 * The prompt is written in the audio's language, as OpenAI recommends.
 */
enum class TranscriptionLanguage(
    val id: String,
    val apiCode: String?,
    val labelRes: Int,
    val instruction: String,
    val vocabularyPrefix: String,
) {
    PORTUGUESE_BRAZIL(
        id = "pt-BR",
        apiCode = "pt",
        labelRes = R.string.language_pt_br,
        instruction = "Transcrição em português do Brasil. Termos técnicos em inglês " +
            "são falados e escritos em inglês.",
        vocabularyPrefix = "Vocabulário: ",
    ),
    AUTOMATIC(
        id = "auto",
        apiCode = null,
        labelRes = R.string.language_auto,
        instruction = "",
        vocabularyPrefix = "Vocabulary: ",
    ),
    ENGLISH(
        id = "en",
        apiCode = "en",
        labelRes = R.string.language_en,
        instruction = "",
        vocabularyPrefix = "Vocabulary: ",
    ),
    SPANISH(
        id = "es",
        apiCode = "es",
        labelRes = R.string.language_es,
        instruction = "Transcripción en español.",
        vocabularyPrefix = "Vocabulario: ",
    ),
    ;

    companion object {
        val DEFAULT = PORTUGUESE_BRAZIL

        fun byId(id: String?): TranscriptionLanguage = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

data class TranscriptionOptions(
    val model: TranscriptionModel = TranscriptionModels.DEFAULT,
    val language: TranscriptionLanguage = TranscriptionLanguage.DEFAULT,
    val vocabulary: List<String> = emptyList(),
) {
    companion object {
        val DEFAULT = TranscriptionOptions()
    }
}

/** The user's personal dictionary: words and names the model tends to get wrong. */
object Vocabulary {
    const val MAX_TERMS = 100
    const val MAX_TERM_LENGTH = 64

    /**
     * Parses free text (one term per line or comma-separated) into clean terms.
     * The API rejects the whole request if a keyword contains `<`, `>`, CR or LF,
     * so those are stripped here.
     */
    fun parse(raw: String): List<String> {
        val seen = HashSet<String>()
        return raw.split('\n', '\r', ',')
            .asSequence()
            .map { it.replace(Regex("[<>]"), " ").replace(Regex("\\s+"), " ").trim() }
            .map { it.take(MAX_TERM_LENGTH).trim() }
            .filter { it.isNotEmpty() }
            .filter { seen.add(it.lowercase()) }
            .take(MAX_TERMS)
            .toList()
    }

    /**
     * Builds the `prompt` field: the language instruction, then (for models without
     * `keywords[]`) the dictionary, then the tail of the previous chunk's text, all
     * within the model's prompt limit. Earlier parts take priority.
     */
    fun prompt(
        model: TranscriptionModel,
        language: TranscriptionLanguage,
        vocabulary: List<String>,
        continuation: String,
    ): String {
        val limit = model.maxPromptChars
        val parts = mutableListOf<String>()
        var used = 0
        fun room() = limit - used - (if (parts.isEmpty()) 0 else 1)

        if (language.instruction.isNotEmpty() && language.instruction.length <= room()) {
            parts += language.instruction
            used += language.instruction.length
        }

        if (!model.supportsKeywords && vocabulary.isNotEmpty()) {
            val glossary = StringBuilder(language.vocabularyPrefix)
            val available = room()
            for (term in vocabulary) {
                val separator = if (glossary.length > language.vocabularyPrefix.length) ", " else ""
                if (glossary.length + separator.length + term.length + 1 > available) break
                glossary.append(separator).append(term)
            }
            if (glossary.length > language.vocabularyPrefix.length) {
                glossary.append('.')
                used += glossary.length + (if (parts.isEmpty()) 0 else 1)
                parts += glossary.toString()
            }
        }

        val tail = room().takeIf { it > 0 }?.let { continuation.takeLast(it).trim() }.orEmpty()
        if (tail.isNotEmpty()) parts += tail
        return parts.joinToString(" ")
    }
}
