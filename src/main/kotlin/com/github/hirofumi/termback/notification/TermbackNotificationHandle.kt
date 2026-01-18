package com.github.hirofumi.termback.notification

/**
 * A handle for managing a notification posted by a [TermbackNotificationChannel].
 *
 * Channel-specific implementation details (e.g., how notifications are rendered
 * or dismissed) are opaque to the caller. The [notification] property provides
 * access to the logical notification content and lifecycle attributes.
 */
interface TermbackNotificationHandle {
    val notification: TermbackNotification

    fun isExpired(): Boolean

    fun expire()
}
