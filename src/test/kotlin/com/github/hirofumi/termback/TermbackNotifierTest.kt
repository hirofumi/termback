package com.github.hirofumi.termback

import com.github.hirofumi.termback.TermbackNotificationRequest.OnNext
import com.github.hirofumi.termback.TermbackNotificationRequest.Suppress
import com.github.hirofumi.termback.TermbackNotifier.NotifyResult
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TermbackNotifierTest {
    private lateinit var notifier: TermbackNotifier
    private lateinit var registry: TermbackSessionRegistry
    private lateinit var application: Application

    @Before
    fun setUp() {
        notifier = TermbackNotifier()
        registry = TermbackSessionRegistry()
        application = mockk(relaxed = true)

        mockkObject(TermbackSessionRegistry)
        every { TermbackSessionRegistry.getInstance() } returns registry

        mockkStatic(ApplicationManager::class)
        every { ApplicationManager.getApplication() } returns application
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createSession(
        project: Project = mockk(relaxed = true),
        content: Content = mockk(relaxed = true),
    ): TermbackSession = TermbackSession(project, content)

    @Test
    fun `notify returns SessionNotFound when session does not exist`() {
        val request =
            TermbackNotificationRequest(
                sessionId = TermbackSessionId.generate(),
                message = "Test message",
            )

        val result = notifier.notify(request)

        assertEquals(NotifyResult.SessionNotFound, result)
    }

    @Test
    fun `notify returns Accepted when session exists`() {
        val session = createSession()
        registry.register(session)

        val request =
            TermbackNotificationRequest(
                sessionId = session.id,
                message = "Test message",
            )

        val result = notifier.notify(request)

        assertEquals(NotifyResult.Accepted, result)
    }

    @Test
    fun `notify returns Accepted with all request options`() {
        val session = createSession()
        registry.register(session)

        val request =
            TermbackNotificationRequest(
                sessionId = session.id,
                message = "Test message",
                title = "Custom Title",
                broadcast = false,
                suppress = Suppress.NONE,
                onNext = OnNext.KEEP,
            )

        val result = notifier.notify(request)

        assertEquals(NotifyResult.Accepted, result)
    }
}
