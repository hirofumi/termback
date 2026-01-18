package com.github.hirofumi.termback.notification

import com.github.hirofumi.termback.NotificationDestination
import com.github.hirofumi.termback.TermbackSettings
import com.github.hirofumi.termback.notification.channels.ide.TermbackIdeNotificationChannel
import com.github.hirofumi.termback.notification.channels.macos.TermbackMacOsNotificationChannel
import com.intellij.openapi.util.SystemInfo
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
        private val macOsChannel by lazy { TermbackMacOsNotificationChannel() }

        fun getInstance(): TermbackNotificationChannel =
            when (TermbackSettings.getInstance().state.notificationDestination) {
                NotificationDestination.IDE -> ideChannel
                NotificationDestination.SYSTEM -> if (SystemInfo.isMac) macOsChannel else ideChannel
            }
    }
}
