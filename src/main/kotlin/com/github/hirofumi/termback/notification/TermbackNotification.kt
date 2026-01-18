package com.github.hirofumi.termback.notification

import com.github.hirofumi.termback.TermbackSession

/**
 * Represents the content and behavior of a notification.
 *
 * This is the logical notification independent of the delivery channel.
 * Channel-specific details (e.g., platform notification objects) are managed by [TermbackNotificationHandle].
 */
class TermbackNotification(
    val session: TermbackSession,
    val title: String,
    val message: String,
    val suppress: TermbackNotificationRequest.Suppress,
    val onNext: TermbackNotificationRequest.OnNext,
) {
    /**
     * Returns the title as displayed in [targetProject].
     * If the notification originated from a different project, the title is prefixed with the source project name.
     */
    fun titleFor(targetProject: Project): String = if (session.project == targetProject) title else "[${session.project.name}] $title"
}
