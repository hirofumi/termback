package com.github.hirofumi.termback

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.content.Content
import com.intellij.util.concurrency.annotations.RequiresEdt

/**
 * Represents a terminal session bound to a Content tab.
 * Thread-safe for notification operations; project and content are immutable after construction.
 */
class TermbackSession(
    val project: Project,
    val content: Content,
) {
    val id = TermbackSessionId.generate()
    private val notifications = mutableListOf<TermbackNotification>()

    /**
     * Navigates to this session's terminal tab and expires suppressed notifications.
     *
     * @return true if the tab was successfully activated, false otherwise.
     */
    @RequiresEdt
    fun navigateToTab(): Boolean {
        if (!activateTab()) return false

        takeSuppressedNotifications(TermbackTabState.VISIBLE_ACTIVE)
            .forEach { it.expire() }

        return true
    }

    @RequiresEdt
    private fun activateTab(): Boolean {
        if (project.isDisposed) return false
        val toolWindow = project.getTerminalToolWindow() ?: return false
        if (content !in toolWindow.contentManager.contentsRecursively) return false

        WindowManager.getInstance().getFrame(project)?.toFront()
        toolWindow.show()
        toolWindow.contentManager.setSelectedContentCB(content, true, true)

        return true
    }

    @RequiresEdt
    fun postNotification(
        title: String,
        message: String,
        suppress: TermbackNotificationRequest.Suppress,
        onNext: TermbackNotificationRequest.OnNext,
        targetProjects: List<Project>,
    ) {
        if (content.manager == null) return

        takeExpiredByNext().forEach { it.expire() }

        val notification =
            TermbackNotification.create(
                session = this,
                baseTitle = title,
                message = message,
                suppress = suppress,
                onNext = onNext,
                targetProjects = targetProjects,
            )

        synchronized(notifications) {
            notifications.add(notification)
        }

        notification.post()
    }

    fun getUnexpiredNotifications(): List<TermbackNotification> {
        synchronized(notifications) {
            // Reuse cleanup logic from takeNotificationsIf; predicate=false means nothing is taken.
            // Removal of already-expired entries is not an observable side effect.
            takeNotificationsIf { false }

            return notifications.toList()
        }
    }

    fun takeAllNotifications(): List<TermbackNotification> = takeNotificationsIf { true }

    fun takeSuppressedNotifications(state: TermbackTabState): List<TermbackNotification> =
        takeNotificationsIf { it.suppress.matches(state) }

    fun takeExpiredByNext(): List<TermbackNotification> = takeNotificationsIf { it.onNext == TermbackNotificationRequest.OnNext.EXPIRE }

    private fun takeNotificationsIf(predicate: (TermbackNotification) -> Boolean): List<TermbackNotification> {
        synchronized(notifications) {
            val result = mutableListOf<TermbackNotification>()
            val iterator = notifications.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.isExpired) {
                    iterator.remove()
                } else if (predicate(entry)) {
                    result.add(entry)
                    iterator.remove()
                }
            }
            return result.toList()
        }
    }
}
