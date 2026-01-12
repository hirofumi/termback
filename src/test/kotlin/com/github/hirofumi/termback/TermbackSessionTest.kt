package com.github.hirofumi.termback

import com.github.hirofumi.termback.TermbackNotificationRequest.OnNext
import com.github.hirofumi.termback.TermbackNotificationRequest.Suppress
import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.After
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
    private val createdNotifications = mutableListOf<TermbackNotification>()

    @Before
    fun setUp() {
        project = mockk()
        content = mockk()
        contentManager = mockk()
        every { content.manager } returns contentManager
        session = TermbackSession(project, content)

        mockkObject(TermbackNotification)
        every {
            TermbackNotification.create(any(), any(), any(), any(), any(), any())
        } answers {
            val suppress = arg<Suppress>(3)
            val onNext = arg<OnNext>(4)
            createMockNotification(suppress, onNext).also { createdNotifications.add(it) }
        }
    }

    @After
    fun tearDown() {
        unmockkObject(TermbackNotification)
        createdNotifications.clear()
    }

    private fun createMockNotification(
        suppress: Suppress = Suppress.WHEN_ACTIVE,
        onNext: OnNext = OnNext.EXPIRE,
    ): TermbackNotification {
        val notification = mockk<TermbackNotification>(relaxed = true)
        every { notification.session } returns session
        every { notification.suppress } returns suppress
        every { notification.onNext } returns onNext
        every { notification.isExpired } returns false
        return notification
    }

    private fun postNotification(
        suppress: Suppress = Suppress.WHEN_ACTIVE,
        onNext: OnNext = OnNext.KEEP,
    ) {
        session.postNotification("title", "message", suppress, onNext, listOf(project))
    }

    @Test
    fun `postNotification stores notification`() {
        postNotification()

        val result = session.takeAllNotifications()
        assertEquals(1, result.size)
        assertEquals(createdNotifications.first(), result.first())
    }

    @Test
    fun `postNotification removes previous EXPIRE notifications from session`() {
        postNotification(onNext = OnNext.EXPIRE)
        val firstNotification = createdNotifications.first()

        postNotification(onNext = OnNext.KEEP)

        val remaining = session.takeAllNotifications()
        assertFalse(remaining.contains(firstNotification))
    }

    @Test
    fun `postNotification preserves previous KEEP notifications`() {
        postNotification(onNext = OnNext.KEEP)
        postNotification(onNext = OnNext.KEEP)

        val result = session.takeAllNotifications()
        assertEquals(2, result.size)
    }

    @Test
    fun `takeSuppressedNotifications with VISIBLE_ACTIVE returns WHEN_ACTIVE and WHEN_VISIBLE notifications`() {
        postNotification(suppress = Suppress.WHEN_ACTIVE)
        postNotification(suppress = Suppress.WHEN_VISIBLE)
        postNotification(suppress = Suppress.NONE)

        val result = session.takeSuppressedNotifications(TermbackTabState.VISIBLE_ACTIVE)

        assertEquals(2, result.size)
        assertTrue(result.any { it.suppress == Suppress.WHEN_ACTIVE })
        assertTrue(result.any { it.suppress == Suppress.WHEN_VISIBLE })
    }

    @Test
    fun `takeSuppressedNotifications with VISIBLE_INACTIVE returns WHEN_VISIBLE notifications`() {
        postNotification(suppress = Suppress.WHEN_ACTIVE)
        postNotification(suppress = Suppress.WHEN_VISIBLE)
        postNotification(suppress = Suppress.NONE)

        val result = session.takeSuppressedNotifications(TermbackTabState.VISIBLE_INACTIVE)

        assertEquals(1, result.size)
        assertEquals(Suppress.WHEN_VISIBLE, result.first().suppress)
    }

    @Test
    fun `takeSuppressedNotifications with NOT_VISIBLE returns empty list`() {
        postNotification(suppress = Suppress.WHEN_ACTIVE)

        val result = session.takeSuppressedNotifications(TermbackTabState.NOT_VISIBLE)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `takeSuppressedNotifications removes returned notifications`() {
        postNotification(suppress = Suppress.WHEN_ACTIVE)
        session.takeSuppressedNotifications(TermbackTabState.VISIBLE_ACTIVE)

        val remaining = session.takeAllNotifications()
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun `takeSuppressedNotifications preserves non-matching notifications`() {
        postNotification(suppress = Suppress.WHEN_ACTIVE)
        postNotification(suppress = Suppress.NONE)

        session.takeSuppressedNotifications(TermbackTabState.VISIBLE_ACTIVE)

        val remaining = session.takeAllNotifications()
        assertEquals(1, remaining.size)
        assertEquals(Suppress.NONE, remaining.first().suppress)
    }

    @Test
    fun `takeAllNotifications returns all notifications`() {
        postNotification(suppress = Suppress.WHEN_ACTIVE)
        postNotification(suppress = Suppress.WHEN_VISIBLE)

        val result = session.takeAllNotifications()

        assertEquals(2, result.size)
    }

    @Test
    fun `takeAllNotifications clears the list`() {
        postNotification()
        session.takeAllNotifications()

        val result = session.takeAllNotifications()
        assertTrue(result.isEmpty())
    }
}
