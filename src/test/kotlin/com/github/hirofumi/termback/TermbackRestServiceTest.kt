package com.github.hirofumi.termback

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.content.ContentFactory
import org.jetbrains.ide.BuiltInServerManager
import java.net.HttpURLConnection
import java.net.URI

/**
 * Tests for [TermbackRestService] HTTP endpoint.
 *
 * These tests run in an actual IntelliJ Platform environment and verify
 * the HTTP endpoint behavior through the full request-response cycle.
 */
class TermbackRestServiceTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        // Start the built-in server and wait for it to be ready
        BuiltInServerManager.getInstance().waitForStart()
    }

    private fun getEndpointUrl(): String {
        val port = BuiltInServerManager.getInstance().port
        return "http://localhost:$port/api/termback"
    }

    private fun sendPost(
        url: String,
        body: String,
    ): HttpResponse {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.use { it.write(body.toByteArray()) }

            val responseCode = connection.responseCode
            val responseBody =
                try {
                    connection.inputStream.bufferedReader().readText()
                } catch (e: Exception) {
                    connection.errorStream?.bufferedReader()?.readText() ?: ""
                }
            HttpResponse(responseCode, responseBody)
        } finally {
            connection.disconnect()
        }
    }

    private data class HttpResponse(
        val code: Int,
        val body: String,
    )

    fun testEndpointReturnsNotFoundForUnknownSession() {
        val url = getEndpointUrl()
        val body = """{"sessionId":"unknown-session-id","message":"Test"}"""

        val response = sendPost(url, body)

        assertEquals(404, response.code)
    }

    fun testEndpointReturnsBadRequestForInvalidJson() {
        val url = getEndpointUrl()
        val body = """not valid json"""

        val response = sendPost(url, body)

        assertEquals(400, response.code)
    }

    fun testEndpointReturnsBadRequestForMissingSessionId() {
        val url = getEndpointUrl()
        val body = """{"message":"Test"}"""

        val response = sendPost(url, body)

        assertEquals(400, response.code)
    }

    fun testEndpointReturnsBadRequestForMissingMessage() {
        val url = getEndpointUrl()
        val body = """{"sessionId":"abc-123"}"""

        val response = sendPost(url, body)

        assertEquals(400, response.code)
    }

    fun testEndpointReturnsBadRequestForEmptySessionId() {
        val url = getEndpointUrl()
        val body = """{"sessionId":"","message":"Test"}"""

        val response = sendPost(url, body)

        assertEquals(400, response.code)
    }

    fun testEndpointReturnsBadRequestForEmptyMessage() {
        val url = getEndpointUrl()
        val body = """{"sessionId":"abc-123","message":""}"""

        val response = sendPost(url, body)

        assertEquals(400, response.code)
    }

    fun testEndpointReturnsAcceptedForValidSessionId() {
        // Register a session first
        val content = ContentFactory.getInstance().createContent(null, "Test", false)
        val session = TermbackSession(project, content)
        TermbackSessionRegistry.getInstance().register(session)

        try {
            val url = getEndpointUrl()
            val body = """{"sessionId":"${session.id.value}","message":"Test notification"}"""

            val response = sendPost(url, body)

            assertEquals(202, response.code)
        } finally {
            // Clean up
            TermbackSessionRegistry.getInstance().unregister(session)
        }
    }
}
