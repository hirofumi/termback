package com.github.hirofumi.termback

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.annotations.RequiresEdt
import java.lang.ref.WeakReference

class TermbackNotification private constructor(
    val session: TermbackSession,
    val title: String,
    val message: String,
    val suppress: TermbackNotificationRequest.Suppress,
    val onNext: TermbackNotificationRequest.OnNext,
    private val entries: List<Entry>,
) {
    /**
     * Returns true if all notifications have been expired or GC'd.
     */
    val isExpired: Boolean
        get() = entries.all { it.notification.get()?.isExpired != false }

    /**
     * Posts all notifications to their respective projects.
     */
    @RequiresEdt
    fun post() {
        entries.forEach { entry ->
            entry.notification.get()?.notify(entry.project)
        }
    }

    fun expire() {
        entries.forEach { it.notification.get()?.expire() }
    }

    companion object {
        private const val GROUP_ID = "com.github.hirofumi.termback"

        @RequiresEdt
        fun create(
            session: TermbackSession,
            baseTitle: String,
            message: String,
            suppress: TermbackNotificationRequest.Suppress,
            onNext: TermbackNotificationRequest.OnNext,
            targetProjects: List<Project>,
        ): TermbackNotification {
            val entries =
                targetProjects.map { targetProject ->
                    val title =
                        if (targetProject == session.project) {
                            baseTitle
                        } else {
                            "[${session.project.name}] $baseTitle"
                        }
                    val raw = createRawNotification(title, message)
                    raw.addAction(
                        NotificationAction.createSimple(TermbackBundle.message("notification.action.show")) {
                            session.navigateToTab()
                        },
                    )
                    Entry(targetProject, WeakReference(raw))
                }
            return TermbackNotification(session, baseTitle, message, suppress, onNext, entries)
        }

        @RequiresEdt
        private fun createRawNotification(
            title: String,
            message: String,
        ): Notification =
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup(GROUP_ID)
                .createNotification(title, message, NotificationType.INFORMATION)
    }

    private data class Entry(
        val project: Project,
        /** WeakReference allows GC if notification is expired externally. */
        val notification: WeakReference<Notification>,
    )
}
