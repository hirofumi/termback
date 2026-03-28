package com.github.hirofumi.termback

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.APP)
class TermbackSessionRegistry {
    private val sessions = ConcurrentHashMap<TermbackSessionId, TermbackSession>()

    fun register(session: TermbackSession): Boolean = sessions.putIfAbsent(session.id, session) == null

    fun unregister(session: TermbackSession) {
        sessions.remove(session.id, session)
    }

    fun findById(sessionId: TermbackSessionId): TermbackSession? = sessions[sessionId]

    fun getAllSessions(): List<TermbackSession> = sessions.values.toList()

    companion object {
        fun getInstance(): TermbackSessionRegistry = ApplicationManager.getApplication().getService(TermbackSessionRegistry::class.java)
    }
}
