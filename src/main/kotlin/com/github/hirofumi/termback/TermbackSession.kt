package com.github.hirofumi.termback

import com.github.hirofumi.termback.notification.TermbackNotificationHandle
import com.github.hirofumi.termback.notification.TermbackNotificationRequest
import com.intellij.openapi.project.Project
import com.intellij.ide.impl.ProjectUtil
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
    private val handles = mutableListOf<TermbackNotificationHandle>()

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

        ProjectUtil.focusProjectWindow(project, stealFocusIfAppInactive = true)
        toolWindow.show()
        toolWindow.contentManager.setSelectedContentCB(content, true, true)

        return true
    }

    fun addNotification(handle: TermbackNotificationHandle) {
        synchronized(handles) {
            handles.add(handle)
        }
    }

    fun getUnexpiredNotifications(): List<TermbackNotificationHandle> {
        synchronized(handles) {
            // Reuse cleanup logic from takeNotificationsIf; predicate=false means nothing is taken.
            // Removal of already-expired entries is not an observable side effect.
            takeNotificationsIf { false }

            return handles.toList()
        }
    }

    fun takeAllNotifications(): List<TermbackNotificationHandle> = takeNotificationsIf { true }

    fun takeSuppressedNotifications(state: TermbackTabState): List<TermbackNotificationHandle> =
        takeNotificationsIf { it.notification.suppress.matches(state) }

    fun takeExpiredByNext(): List<TermbackNotificationHandle> =
        takeNotificationsIf { it.notification.onNext == TermbackNotificationRequest.OnNext.EXPIRE }

    private fun takeNotificationsIf(predicate: (TermbackNotificationHandle) -> Boolean): List<TermbackNotificationHandle> {
        synchronized(handles) {
            val result = mutableListOf<TermbackNotificationHandle>()
            val iterator = handles.iterator()
            while (iterator.hasNext()) {
                val handle = iterator.next()
                if (handle.isExpired()) {
                    iterator.remove()
                } else if (predicate(handle)) {
                    result.add(handle)
                    iterator.remove()
                }
            }
            return result.toList()
        }
    }
}
