package com.github.hirofumi.termback

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager

@Service(Service.Level.APP)
class TermbackNotifier {
    sealed interface NotifyResult {
        /** Request was accepted for processing. Notification may be skipped based on suppress conditions. */
        data object Accepted : NotifyResult

        data object SessionNotFound : NotifyResult
    }

    fun notify(request: TermbackNotificationRequest): NotifyResult {
        val session =
            TermbackSessionRegistry.getInstance().findById(request.sessionId)
                ?: return NotifyResult.SessionNotFound

        ApplicationManager.getApplication().invokeLater({
            if (shouldSkipNotification(session, request.suppress)) return@invokeLater

            val title = request.title ?: session.content.displayName
            val targetProjects = ProjectManager.getInstance().availableProjects.toList()

            session.postNotification(
                title = title,
                message = request.message,
                suppress = request.suppress,
                onNext = request.onNext,
                targetProjects = targetProjects,
            )
        }, session.project.disposed)

        return NotifyResult.Accepted
    }

    fun expireSessionNotifications(session: TermbackSession) {
        session.takeAllNotifications().forEach { it.expire() }
    }

    fun expireSuppressedNotifications(project: Project) {
        ApplicationManager.getApplication().invokeLater({
            val toolWindow = project.getTerminalToolWindow() ?: return@invokeLater
            for (content in toolWindow.contentManager.contentsRecursively) {
                val session = TermbackSessionRegistry.getInstance().findByContent(content) ?: continue
                val state = toolWindow.getTabState(content)
                session.takeSuppressedNotifications(state).forEach { it.expire() }
            }
        }, project.disposed)
    }

    private fun shouldSkipNotification(
        session: TermbackSession,
        suppress: TermbackNotificationRequest.Suppress,
    ): Boolean {
        val frame = WindowManager.getInstance().getFrame(session.project) ?: return false
        if (!frame.isActive) return false
        val toolWindow = session.project.getTerminalToolWindow() ?: return false
        return suppress.matches(toolWindow.getTabState(session.content))
    }

    companion object {
        fun getInstance(): TermbackNotifier = ApplicationManager.getApplication().getService(TermbackNotifier::class.java)
    }
}
