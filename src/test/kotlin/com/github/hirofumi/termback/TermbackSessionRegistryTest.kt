package com.github.hirofumi.termback

import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TermbackSessionRegistryTest {
    private lateinit var registry: TermbackSessionRegistry

    @Before
    fun setUp() {
        registry = TermbackSessionRegistry()
    }

    private fun createSession(
        project: Project = mockk(),
        content: Content = mockk(),
    ): TermbackSession = TermbackSession(TermbackSessionId.generate(), project, content)

    @Test
    fun `register and findById returns the session`() {
        val session = createSession()

        assertTrue(registry.register(session))

        assertSame(session, registry.findById(session.id))
    }

    @Test
    fun `register ignores a duplicate session id`() {
        val sessionId = TermbackSessionId.generate()
        val session1 = TermbackSession(sessionId, mockk(), mockk())
        val session2 = TermbackSession(sessionId, mockk(), mockk())

        assertTrue(registry.register(session1))
        assertFalse(registry.register(session2))
        assertSame(session1, registry.findById(sessionId))
    }

    @Test
    fun `findById returns null for unknown id`() {
        val unknownId = TermbackSessionId.generate()

        assertNull(registry.findById(unknownId))
    }

    @Test
    fun `unregister removes the session`() {
        val session = createSession()
        registry.register(session)

        registry.unregister(session)

        assertNull(registry.findById(session.id))
    }

    @Test
    fun `multiple sessions can be registered`() {
        val session1 = createSession()
        val session2 = createSession()

        assertTrue(registry.register(session1))
        assertTrue(registry.register(session2))

        assertSame(session1, registry.findById(session1.id))
        assertSame(session2, registry.findById(session2.id))
    }

    @Test
    fun `unregister does not affect other sessions`() {
        val session1 = createSession()
        val session2 = createSession()
        registry.register(session1)
        registry.register(session2)

        registry.unregister(session1)

        assertNull(registry.findById(session1.id))
        assertSame(session2, registry.findById(session2.id))
    }

    @Test
    fun `getAllSessions returns all registered sessions`() {
        val session1 = createSession()
        val session2 = createSession()
        registry.register(session1)
        registry.register(session2)

        val result = registry.getAllSessions()

        assertEquals(2, result.size)
        assertEquals(setOf(session1, session2), result.toSet())
    }
}
