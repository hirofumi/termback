package com.github.hirofumi.termback.notification.channels.macos

import com.github.hirofumi.termback.notification.TermbackNotification
import com.github.hirofumi.termback.notification.TermbackNotificationChannel
import com.github.hirofumi.termback.notification.TermbackNotificationHandle
import com.intellij.util.concurrency.annotations.RequiresEdt

internal class TermbackMacOsNotificationChannel : TermbackNotificationChannel {
    @RequiresEdt
    override fun post(notification: TermbackNotification): TermbackNotificationHandle =
        TermbackMacOsNotificationHandle.postToSystem(notification)
}
