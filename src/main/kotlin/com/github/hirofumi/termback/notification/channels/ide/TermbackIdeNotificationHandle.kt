package com.github.hirofumi.termback.notification.channels.ide

import com.github.hirofumi.termback.TermbackBundle
import com.github.hirofumi.termback.availableProjects
import com.github.hirofumi.termback.notification.TermbackNotification
import com.github.hirofumi.termback.notification.TermbackNotificationHandle
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.concurrency.annotations.RequiresEdt
import java.lang.ref.WeakReference

internal class TermbackIdeNotificationHandle private constructor(
    override val notification: TermbackNotification,
    private val entries: List<Entry>,
) : TermbackNotificationHandle {
    /**
     * Returns true if all notifications have been expired or GC'd.
     */
    override fun isExpired(): Boolean = entries.all { it.ideNotificationRef.get()?.isExpired != false }

    override fun expire() {
        entries.forEach { it.ideNotificationRef.get()?.expire() }
    }

    companion object {
        private const val GROUP_ID = "com.github.hirofumi.termback"

        @RequiresEdt
        fun postToIde(notification: TermbackNotification): TermbackIdeNotificationHandle {
            val entries =
                ProjectManager.getInstance().availableProjects.map { targetProject ->
                    val ideNotification = createIdeNotification(targetProject, notification)
                    ideNotification.notify(targetProject)
                    Entry(targetProject, WeakReference(ideNotification))
                }

            return TermbackIdeNotificationHandle(notification, entries)
        }

        @RequiresEdt
        private fun createIdeNotification(
            targetProject: Project,
            notification: TermbackNotification,
        ): Notification {
            val ideNotification =
                NotificationGroupManager
                    .getInstance()
                    .getNotificationGroup(GROUP_ID)
                    .createNotification(notification.titleFor(targetProject), notification.message, NotificationType.INFORMATION)

            ideNotification.addAction(
                NotificationAction.createSimple(TermbackBundle.message("notification.action.show")) {
                    notification.session.navigateToTab()
                },
            )

            return ideNotification
        }
    }

    private data class Entry(
        val project: Project,
        /** WeakReference allows GC if notification is expired externally. */
        val ideNotificationRef: WeakReference<Notification>,
    )
}
