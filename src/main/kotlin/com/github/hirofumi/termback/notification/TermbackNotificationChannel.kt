package com.github.hirofumi.termback.notification

import com.github.hirofumi.termback.notification.channels.ide.TermbackIdeNotificationChannel
import com.intellij.util.concurrency.annotations.RequiresEdt

/**
 * A channel that posts notifications.
 *
 * Channels are responsible only for displaying notifications.
 * Routing and lifecycle management are handled by [TermbackNotifier].
 */
interface TermbackNotificationChannel {
    @RequiresEdt
    fun post(notification: TermbackNotification): TermbackNotificationHandle

    companion object {
        private val ideChannel = TermbackIdeNotificationChannel()

        fun getInstance(): TermbackNotificationChannel = TermbackIdeNotificationChannel()
    }
}
