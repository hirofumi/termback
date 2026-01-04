package com.github.hirofumi.termback

import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import io.mockk.mockk
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
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
    ): TermbackSession = TermbackSession(project, content)

    @Test
    fun `register and findById returns the session`() {
        val session = createSession()

        registry.register(session)

        assertSame(session, registry.findById(session.id))
    }

    @Test
    fun `findById returns null for unknown id`() {
        val unknownId = TermbackSessionId.generate()

        assertNull(registry.findById(unknownId))
    }

    @Test
    fun `findByContent returns the session`() {
        val content = mockk<Content>()
        val session = createSession(content = content)

        registry.register(session)

        assertSame(session, registry.findByContent(content))
    }

    @Test
    fun `findByContent returns null for unknown content`() {
        val session = createSession()
        registry.register(session)

        val unknownContent = mockk<Content>()

        assertNull(registry.findByContent(unknownContent))
    }

    @Test
    fun `findByContent uses reference equality`() {
        val content = mockk<Content>()
        val session = createSession(content = content)
        registry.register(session)

        // Different mock instance, even if it has the same behavior
        val differentContent = mockk<Content>()

        assertNull(registry.findByContent(differentContent))
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

        registry.register(session1)
        registry.register(session2)

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
}
