package com.github.hirofumi.termback

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TermbackSessionIdTest {
    private val uuidPattern = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")

    @Test
    fun `generate creates valid UUID format`() {
        val sessionId = TermbackSessionId.generate()

        assertTrue(
            "Expected UUID format (8-4-4-4-12), got: ${sessionId.value}",
            uuidPattern.matches(sessionId.value),
        )
    }

    @Test
    fun `generate creates unique IDs`() {
        val ids = (1..100).map { TermbackSessionId.generate() }
        val uniqueIds = ids.map { it.value }.toSet()

        assertEquals("All generated IDs should be unique", 100, uniqueIds.size)
    }

    @Test
    fun `toString returns value`() {
        val value = "test-session-id"
        val sessionId = TermbackSessionId(value)

        assertEquals(value, sessionId.toString())
    }

    @Test
    fun `serialization round-trip preserves value`() {
        val original = TermbackSessionId.generate()
        val json = Json.encodeToString(TermbackSessionId.serializer(), original)
        val deserialized = Json.decodeFromString(TermbackSessionId.serializer(), json)

        assertEquals(original, deserialized)
        assertEquals(original.value, deserialized.value)
    }
}
