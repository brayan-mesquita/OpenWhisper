package com.openwhisper.android

import java.util.concurrent.atomic.AtomicReference

/**
 * In-process hand-off of a dictation failure from the accessibility service to
 * MainActivity. Both run in the same process, so the message never travels in an
 * intent extra that another app could forge against the exported activity.
 */
object DictationRecovery {
    private val pending = AtomicReference<String?>(null)

    fun post(message: String) {
        pending.set(message)
    }

    fun take(): String? = pending.getAndSet(null)
}
