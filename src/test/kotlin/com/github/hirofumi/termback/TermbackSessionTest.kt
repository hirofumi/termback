package com.github.hirofumi.termback

import com.github.hirofumi.termback.notification.TermbackNotification
import com.github.hirofumi.termback.notification.TermbackNotificationHandle
import com.github.hirofumi.termback.notification.TermbackNotificationRequest.OnNext
import com.github.hirofumi.termback.notification.TermbackNotificationRequest.Suppress
import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TermbackSessionTest {
    private lateinit var session: TermbackSession
    private lateinit var project: Project
    private lateinit var content: Content
    private lateinit var contentManager: ContentManager

    @Before
    fun setUp() {
        project = mockk()
        content = mockk()
        contentManager = mockk()
        every { content.manager } returns contentManager
        session = TermbackSession(project, content)
    }

    private fun createMockNotification(
        suppress: Suppress = Suppress.WHEN_ACTIVE,
        onNext: OnNext = OnNext.EXPIRE,
        isExpired: Boolean = false,
    ): TermbackNotificationHandle {
        val notification = mockk<TermbackNotification>()
        every { notification.session } returns session
        every { notification.suppress } returns suppress
        every { notification.onNext } returns onNext

        val handle = mockk<TermbackNotificationHandle>(relaxed = true)
        every { handle.notification } returns notification
        every { handle.isExpired() } returns isExpired
        return handle
    }

    @Test
    fun `addNotification stores notification`() {
        val handle = createMockNotification()
        session.addNotification(handle)

        val result = session.takeAllNotifications()
        assertEquals(1, result.size)
        assertEquals(handle, result.first())
    }

    @Test
    fun `takeExpiredByNext returns EXPIRE notifications`() {
        val expireHandle = createMockNotification(onNext = OnNext.EXPIRE)
        val keepHandle = createMockNotification(onNext = OnNext.KEEP)
        session.addNotification(expireHandle)
        session.addNotification(keepHandle)

        val result = session.takeExpiredByNext()

        assertEquals(1, result.size)
        assertEquals(expireHandle, result.first())
    }

    @Test
    fun `takeExpiredByNext preserves KEEP notifications`() {
        val expireHandle = createMockNotification(onNext = OnNext.EXPIRE)
        val keepHandle = createMockNotification(onNext = OnNext.KEEP)
        session.addNotification(expireHandle)
        session.addNotification(keepHandle)

        session.takeExpiredByNext()

        val remaining = session.takeAllNotifications()
        assertEquals(1, remaining.size)
        assertEquals(keepHandle, remaining.first())
    }

    @Test
    fun `takeSuppressedNotifications with VISIBLE_ACTIVE returns WHEN_ACTIVE and WHEN_VISIBLE notifications`() {
        val activeHandle = createMockNotification(suppress = Suppress.WHEN_ACTIVE)
        val visibleHandle = createMockNotification(suppress = Suppress.WHEN_VISIBLE)
        val noneHandle = createMockNotification(suppress = Suppress.NONE)
        session.addNotification(activeHandle)
        session.addNotification(visibleHandle)
        session.addNotification(noneHandle)

        val result = session.takeSuppressedNotifications(TermbackTabState.VISIBLE_ACTIVE)

        assertEquals(2, result.size)
        assertTrue(result.any { it.notification.suppress == Suppress.WHEN_ACTIVE })
        assertTrue(result.any { it.notification.suppress == Suppress.WHEN_VISIBLE })
    }

    @Test
    fun `takeSuppressedNotifications with VISIBLE_INACTIVE returns WHEN_VISIBLE notifications`() {
        val activeHandle = createMockNotification(suppress = Suppress.WHEN_ACTIVE)
        val visibleHandle = createMockNotification(suppress = Suppress.WHEN_VISIBLE)
        val noneHandle = createMockNotification(suppress = Suppress.NONE)
        session.addNotification(activeHandle)
        session.addNotification(visibleHandle)
        session.addNotification(noneHandle)

        val result = session.takeSuppressedNotifications(TermbackTabState.VISIBLE_INACTIVE)

        assertEquals(1, result.size)
        assertEquals(Suppress.WHEN_VISIBLE, result.first().notification.suppress)
    }

    @Test
    fun `takeSuppressedNotifications with NOT_VISIBLE returns empty list`() {
        val handle = createMockNotification(suppress = Suppress.WHEN_ACTIVE)
        session.addNotification(handle)

        val result = session.takeSuppressedNotifications(TermbackTabState.NOT_VISIBLE)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `takeSuppressedNotifications removes returned notifications`() {
        val handle = createMockNotification(suppress = Suppress.WHEN_ACTIVE)
        session.addNotification(handle)

        session.takeSuppressedNotifications(TermbackTabState.VISIBLE_ACTIVE)

        val remaining = session.takeAllNotifications()
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun `takeSuppressedNotifications preserves non-matching notifications`() {
        val activeHandle = createMockNotification(suppress = Suppress.WHEN_ACTIVE)
        val noneHandle = createMockNotification(suppress = Suppress.NONE)
        session.addNotification(activeHandle)
        session.addNotification(noneHandle)

        session.takeSuppressedNotifications(TermbackTabState.VISIBLE_ACTIVE)

        val remaining = session.takeAllNotifications()
        assertEquals(1, remaining.size)
        assertEquals(Suppress.NONE, remaining.first().notification.suppress)
    }

    @Test
    fun `takeAllNotifications returns all notifications`() {
        val handle1 = createMockNotification(suppress = Suppress.WHEN_ACTIVE)
        val handle2 = createMockNotification(suppress = Suppress.WHEN_VISIBLE)
        session.addNotification(handle1)
        session.addNotification(handle2)

        val result = session.takeAllNotifications()

        assertEquals(2, result.size)
    }

    @Test
    fun `takeAllNotifications clears the list`() {
        val handle = createMockNotification()
        session.addNotification(handle)
        session.takeAllNotifications()

        val result = session.takeAllNotifications()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getUnexpiredNotifications filters out expired notifications`() {
        val unexpiredHandle = createMockNotification(isExpired = false)
        val expiredHandle = createMockNotification(isExpired = true)
        session.addNotification(unexpiredHandle)
        session.addNotification(expiredHandle)

        val result = session.getUnexpiredNotifications()

        assertEquals(1, result.size)
        assertFalse(result.first().isExpired())
    }
}
