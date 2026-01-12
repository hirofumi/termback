package com.github.hirofumi.termback

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
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
            if (session.content.manager == null) return@invokeLater // Content was disposed

            val title = request.title ?: session.content.displayName

            val targetProjects =
                if (request.broadcast) {
                    ProjectManager.getInstance().availableProjects
                } else {
                    listOf(session.project)
                }

            session
                .takeExpiredByNext()
                .forEach { it.expire() }

            targetProjects.forEach { targetProject ->
                val notification =
                    createNotification(
                        session,
                        if (targetProject == session.project) title else "[${session.project.name}] $title",
                        request.message,
                    )
                session.addNotification(notification, request.suppress, request.onNext)
                notification.notify(targetProject)
            }
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

    private fun createNotification(
        session: TermbackSession,
        title: String,
        message: String,
    ): Notification {
        val notification =
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup("com.github.hirofumi.termback")
                .createNotification(title, message, NotificationType.INFORMATION)

        notification.addAction(
            NotificationAction.createSimple(TermbackBundle.message("notification.action.show")) {
                if (session.project.isDisposed) {
                    notification.expire()
                    return@createSimple
                }

                val toolWindow = session.project.getTerminalToolWindow() ?: return@createSimple

                if (session.content !in toolWindow.contentManager.contentsRecursively) {
                    notification.expire()
                    return@createSimple
                }

                WindowManager.getInstance().getFrame(session.project)?.toFront()
                toolWindow.show()
                toolWindow.contentManager.setSelectedContentCB(session.content, true, true)

                session
                    .takeSuppressedNotifications(TermbackTabState.VISIBLE_ACTIVE)
                    .forEach { it.expire() }
            },
        )

        return notification
    }

    companion object {
        fun getInstance(): TermbackNotifier = ApplicationManager.getApplication().getService(TermbackNotifier::class.java)
    }
}
