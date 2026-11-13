package com.openwhisper.android.settings

import android.content.Context
import com.openwhisper.android.BuildConfig
import com.openwhisper.android.transcription.TranscriptionLanguage
import com.openwhisper.android.transcription.TranscriptionModel
import com.openwhisper.android.transcription.TranscriptionModels
import com.openwhisper.android.transcription.TranscriptionOptions
import com.openwhisper.android.transcription.Vocabulary

class SettingsRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("openwhisper_settings", 0)

    var demoMode: Boolean
        get() = preferences.getBoolean("demo_mode", BuildConfig.DEMO_TRANSCRIBER)
        set(value) = preferences.edit().putBoolean("demo_mode", value).apply()

    var transcriptionModel: TranscriptionModel
        get() = TranscriptionModels.byId(preferences.getString("transcription_model", null))
        set(value) = preferences.edit().putString("transcription_model", value.id).apply()

    var transcriptionLanguage: TranscriptionLanguage
        get() = TranscriptionLanguage.byId(preferences.getString("transcription_language", null))
        set(value) = preferences.edit().putString("transcription_language", value.id).apply()

    /** Raw dictionary text as typed by the user (one term per line). */
    var vocabularyText: String
        get() = preferences.getString("vocabulary", "").orEmpty()
        set(value) = preferences.edit().putString("vocabulary", value).apply()

    fun transcriptionOptions(): TranscriptionOptions = TranscriptionOptions(
        model = transcriptionModel,
        language = transcriptionLanguage,
        vocabulary = Vocabulary.parse(vocabularyText),
    )
}
