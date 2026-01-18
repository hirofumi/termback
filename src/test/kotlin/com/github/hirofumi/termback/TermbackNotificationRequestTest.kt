package com.github.hirofumi.termback

import com.github.hirofumi.termback.TermbackNotificationRequest.OnNext
import com.github.hirofumi.termback.TermbackNotificationRequest.ParseResult
import com.github.hirofumi.termback.TermbackNotificationRequest.Suppress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TermbackNotificationRequestTest {
    // region: Valid JSON parsing

    @Test
    fun `parse valid JSON with required fields only`() {
        val json = """{"sessionId":"abc-123","message":"Hello"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Success)
        val request = (result as ParseResult.Success).request
        assertEquals("abc-123", request.sessionId.value)
        assertEquals("Hello", request.message)
    }

    @Test
    fun `parse valid JSON with all fields`() {
        val json =
            """
            {
                "sessionId": "abc-123",
                "message": "Hello",
                "title": "Test Title",
                "suppress": "none",
                "onNext": "keep"
            }
            """.trimIndent()
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Success)
        val request = (result as ParseResult.Success).request
        assertEquals("abc-123", request.sessionId.value)
        assertEquals("Hello", request.message)
        assertEquals("Test Title", request.title)
        assertEquals(Suppress.NONE, request.suppress)
        assertEquals(OnNext.KEEP, request.onNext)
    }

    @Test
    fun `parse JSON with unknown fields is ignored`() {
        val json = """{"sessionId":"abc-123","message":"Hello","unknownField":"value","anotherUnknown":123}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Success)
        val request = (result as ParseResult.Success).request
        assertEquals("abc-123", request.sessionId.value)
        assertEquals("Hello", request.message)
    }

    // endregion

    // region: Validation errors

    @Test
    fun `parse JSON with empty sessionId returns error`() {
        val json = """{"sessionId":"","message":"Hello"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Error)
        assertEquals("sessionId must be a non-empty string", (result as ParseResult.Error).reason)
    }

    @Test
    fun `parse JSON with blank sessionId returns error`() {
        val json = """{"sessionId":"   ","message":"Hello"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Error)
        assertEquals("sessionId must be a non-empty string", (result as ParseResult.Error).reason)
    }

    @Test
    fun `parse JSON with empty message returns error`() {
        val json = """{"sessionId":"abc-123","message":""}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Error)
        assertEquals("message must be a non-empty string", (result as ParseResult.Error).reason)
    }

    @Test
    fun `parse JSON with blank message returns error`() {
        val json = """{"sessionId":"abc-123","message":"   "}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Error)
        assertEquals("message must be a non-empty string", (result as ParseResult.Error).reason)
    }

    // endregion

    // region: Parse errors

    @Test
    fun `parse invalid JSON returns error`() {
        val json = """not valid json"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Error)
    }

    @Test
    fun `parse JSON with missing sessionId returns error`() {
        val json = """{"message":"Hello"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Error)
    }

    @Test
    fun `parse JSON with missing message returns error`() {
        val json = """{"sessionId":"abc-123"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Error)
    }

    @Test
    fun `parse JSON with invalid suppress returns error`() {
        val json = """{"sessionId":"abc-123","message":"Hello","suppress":"invalid"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Error)
    }

    @Test
    fun `parse JSON with invalid onNext returns error`() {
        val json = """{"sessionId":"abc-123","message":"Hello","onNext":"invalid"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Error)
    }

    // endregion

    // region: Default values

    @Test
    fun `default title is null`() {
        val json = """{"sessionId":"abc-123","message":"Hello"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Success)
        val request = (result as ParseResult.Success).request
        assertNull(request.title)
    }

    @Test
    fun `default suppress is WHEN_ACTIVE`() {
        val json = """{"sessionId":"abc-123","message":"Hello"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Success)
        val request = (result as ParseResult.Success).request
        assertEquals(Suppress.WHEN_ACTIVE, request.suppress)
    }

    @Test
    fun `default onNext is EXPIRE`() {
        val json = """{"sessionId":"abc-123","message":"Hello"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Success)
        val request = (result as ParseResult.Success).request
        assertEquals(OnNext.EXPIRE, request.onNext)
    }

    // endregion

    // region: Enum deserialization

    @Test
    fun `Suppress NONE deserializes from none`() {
        val json = """{"sessionId":"abc-123","message":"Hello","suppress":"none"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Success)
        assertEquals(Suppress.NONE, (result as ParseResult.Success).request.suppress)
    }

    @Test
    fun `Suppress WHEN_ACTIVE deserializes from whenActive`() {
        val json = """{"sessionId":"abc-123","message":"Hello","suppress":"whenActive"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Success)
        assertEquals(Suppress.WHEN_ACTIVE, (result as ParseResult.Success).request.suppress)
    }

    @Test
    fun `Suppress WHEN_VISIBLE deserializes from whenVisible`() {
        val json = """{"sessionId":"abc-123","message":"Hello","suppress":"whenVisible"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Success)
        assertEquals(Suppress.WHEN_VISIBLE, (result as ParseResult.Success).request.suppress)
    }

    @Test
    fun `OnNext KEEP deserializes from keep`() {
        val json = """{"sessionId":"abc-123","message":"Hello","onNext":"keep"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Success)
        assertEquals(OnNext.KEEP, (result as ParseResult.Success).request.onNext)
    }

    @Test
    fun `OnNext EXPIRE deserializes from expire`() {
        val json = """{"sessionId":"abc-123","message":"Hello","onNext":"expire"}"""
        val result = TermbackNotificationRequest.parse(json)

        assertTrue(result is ParseResult.Success)
        assertEquals(OnNext.EXPIRE, (result as ParseResult.Success).request.onNext)
    }

    // endregion
}
