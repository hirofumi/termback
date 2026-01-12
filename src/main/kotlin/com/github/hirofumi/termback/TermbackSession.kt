package com.github.hirofumi.termback

import com.intellij.notification.Notification
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.content.Content
import com.intellij.util.concurrency.annotations.RequiresEdt
import java.lang.ref.WeakReference

/**
 * Represents a terminal session bound to a Content tab.
 * Thread-safe for notification operations; project and content are immutable after construction.
 */
class TermbackSession(
    val project: Project,
    val content: Content,
) {
    val id = TermbackSessionId.generate()
    private val notifications = mutableListOf<NotificationEntry>()

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

    fun addNotification(
        notification: Notification,
        suppress: TermbackNotificationRequest.Suppress,
        onNext: TermbackNotificationRequest.OnNext,
    ) {
        synchronized(notifications) {
            notifications.add(NotificationEntry(WeakReference(notification), suppress, onNext))
        }
    }

    fun takeAllNotifications(): List<Notification> = takeNotificationsIf { true }

    fun takeSuppressedNotifications(state: TermbackTabState): List<Notification> = takeNotificationsIf { it.suppress.matches(state) }

    fun takeExpiredByNext(): List<Notification> = takeNotificationsIf { it.onNext == TermbackNotificationRequest.OnNext.EXPIRE }

    private fun takeNotificationsIf(predicate: (NotificationEntry) -> Boolean): List<Notification> {
        synchronized(notifications) {
            val result = mutableListOf<Notification>()
            val iterator = notifications.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val notification = entry.notificationRef.get()
                if (notification == null) {
                    iterator.remove()
                } else if (predicate(entry)) {
                    result.add(notification)
                    iterator.remove()
                }
            }
            return result.toList()
        }
    }

    private data class NotificationEntry(
        /** WeakReference allows GC if expired externally; stale entries are lazily cleaned in takeNotificationsIf. */
        val notificationRef: WeakReference<Notification>,
        val suppress: TermbackNotificationRequest.Suppress,
        val onNext: TermbackNotificationRequest.OnNext,
    )
}
