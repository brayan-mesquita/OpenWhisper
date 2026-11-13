package com.openwhisper.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DictationRecoveryTest {
    @Test
    fun deliversAPostedMessageExactlyOnce() {
        DictationRecovery.post("Microphone unavailable")
        assertEquals("Microphone unavailable", DictationRecovery.take())
        assertNull(DictationRecovery.take())
    }
}
