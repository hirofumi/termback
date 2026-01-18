package com.github.hirofumi.termback.notification.channels.ide

import com.github.hirofumi.termback.notification.TermbackNotification
import com.github.hirofumi.termback.notification.TermbackNotificationChannel
import com.github.hirofumi.termback.notification.TermbackNotificationHandle
import com.intellij.util.concurrency.annotations.RequiresEdt

internal class TermbackIdeNotificationChannel : TermbackNotificationChannel {
    @RequiresEdt
    override fun post(notification: TermbackNotification): TermbackNotificationHandle =
        TermbackIdeNotificationHandle.postToIde(notification)
}
