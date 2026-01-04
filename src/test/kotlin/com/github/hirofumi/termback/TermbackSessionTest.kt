package com.github.hirofumi.termback

import com.github.hirofumi.termback.TermbackNotificationRequest.OnNext
import com.github.hirofumi.termback.TermbackNotificationRequest.Suppress
import com.intellij.notification.Notification
import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TermbackSessionTest {
    private lateinit var session: TermbackSession
    private lateinit var project: Project
    private lateinit var content: Content

    @Before
    fun setUp() {
        project = mockk()
        content = mockk()
        session = TermbackSession(project, content)
    }

    private fun createNotification(): Notification = mockk()

    @Test
    fun `addNotification stores notification`() {
        val notification = createNotification()

        session.addNotification(notification, Suppress.WHEN_ACTIVE, OnNext.EXPIRE)

        val result = session.takeAllNotifications()
        assertEquals(listOf(notification), result)
    }

    @Test
    fun `takeExpiredByNext returns notifications with EXPIRE onNext`() {
        val notification1 = createNotification()
        val notification2 = createNotification()

        session.addNotification(notification1, Suppress.WHEN_ACTIVE, OnNext.EXPIRE)
        session.addNotification(notification2, Suppress.WHEN_ACTIVE, OnNext.KEEP)

        val result = session.takeExpiredByNext()

        assertEquals(listOf(notification1), result)
    }

    @Test
    fun `takeExpiredByNext removes returned notifications`() {
        val notification = createNotification()

        session.addNotification(notification, Suppress.WHEN_ACTIVE, OnNext.EXPIRE)
        session.takeExpiredByNext()

        val remaining = session.takeAllNotifications()
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun `takeExpiredByNext preserves KEEP notifications`() {
        val notification = createNotification()

        session.addNotification(notification, Suppress.WHEN_ACTIVE, OnNext.KEEP)
        session.takeExpiredByNext()

        val remaining = session.takeAllNotifications()
        assertEquals(listOf(notification), remaining)
    }

    @Test
    fun `takeSuppressedNotifications with VISIBLE_ACTIVE returns WHEN_ACTIVE and WHEN_VISIBLE notifications`() {
        val notification1 = createNotification()
        val notification2 = createNotification()
        val notification3 = createNotification()

        session.addNotification(notification1, Suppress.WHEN_ACTIVE, OnNext.KEEP)
        session.addNotification(notification2, Suppress.WHEN_VISIBLE, OnNext.KEEP)
        session.addNotification(notification3, Suppress.NONE, OnNext.KEEP)

        val result = session.takeSuppressedNotifications(TermbackTabState.VISIBLE_ACTIVE)

        assertEquals(2, result.size)
        assertTrue(result.contains(notification1))
        assertTrue(result.contains(notification2))
    }

    @Test
    fun `takeSuppressedNotifications with VISIBLE_INACTIVE returns WHEN_VISIBLE notifications`() {
        val notification1 = createNotification()
        val notification2 = createNotification()
        val notification3 = createNotification()

        session.addNotification(notification1, Suppress.WHEN_ACTIVE, OnNext.KEEP)
        session.addNotification(notification2, Suppress.WHEN_VISIBLE, OnNext.KEEP)
        session.addNotification(notification3, Suppress.NONE, OnNext.KEEP)

        val result = session.takeSuppressedNotifications(TermbackTabState.VISIBLE_INACTIVE)

        assertEquals(listOf(notification2), result)
    }

    @Test
    fun `takeSuppressedNotifications with NOT_VISIBLE returns empty list`() {
        val notification = createNotification()

        session.addNotification(notification, Suppress.WHEN_ACTIVE, OnNext.KEEP)

        val result = session.takeSuppressedNotifications(TermbackTabState.NOT_VISIBLE)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `takeSuppressedNotifications removes returned notifications`() {
        val notification = createNotification()

        session.addNotification(notification, Suppress.WHEN_ACTIVE, OnNext.KEEP)
        session.takeSuppressedNotifications(TermbackTabState.VISIBLE_ACTIVE)

        val remaining = session.takeAllNotifications()
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun `takeSuppressedNotifications preserves non-matching notifications`() {
        val notification1 = createNotification()
        val notification2 = createNotification()

        session.addNotification(notification1, Suppress.WHEN_ACTIVE, OnNext.KEEP)
        session.addNotification(notification2, Suppress.NONE, OnNext.KEEP)

        session.takeSuppressedNotifications(TermbackTabState.VISIBLE_ACTIVE)

        val remaining = session.takeAllNotifications()
        assertEquals(listOf(notification2), remaining)
    }

    @Test
    fun `takeAllNotifications returns all notifications`() {
        val notification1 = createNotification()
        val notification2 = createNotification()

        session.addNotification(notification1, Suppress.WHEN_ACTIVE, OnNext.EXPIRE)
        session.addNotification(notification2, Suppress.WHEN_VISIBLE, OnNext.KEEP)

        val result = session.takeAllNotifications()

        assertEquals(2, result.size)
        assertTrue(result.contains(notification1))
        assertTrue(result.contains(notification2))
    }

    @Test
    fun `takeAllNotifications clears the list`() {
        val notification = createNotification()

        session.addNotification(notification, Suppress.WHEN_ACTIVE, OnNext.EXPIRE)
        session.takeAllNotifications()

        val result = session.takeAllNotifications()
        assertTrue(result.isEmpty())
    }
}
