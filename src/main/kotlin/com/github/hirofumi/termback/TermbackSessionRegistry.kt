package com.github.hirofumi.termback

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.ui.content.Content
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.APP)
class TermbackSessionRegistry {
    private val sessions = ConcurrentHashMap<TermbackSessionId, TermbackSession>()

    fun register(session: TermbackSession) {
        sessions[session.id] = session
    }

    fun unregister(session: TermbackSession) {
        sessions.remove(session.id)
    }

    fun findById(sessionId: TermbackSessionId): TermbackSession? = sessions[sessionId]

    // O(n) linear search is acceptable; terminal tab count is typically small (< 100).
    fun findByContent(content: Content): TermbackSession? = sessions.values.find { it.content === content }

    fun getAllSessions(): List<TermbackSession> = sessions.values.toList()

    companion object {
        fun getInstance(): TermbackSessionRegistry = ApplicationManager.getApplication().getService(TermbackSessionRegistry::class.java)
    }
}
