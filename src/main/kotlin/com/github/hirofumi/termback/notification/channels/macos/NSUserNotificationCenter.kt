package com.github.hirofumi.termback.notification.channels.macos

import com.intellij.ui.mac.foundation.Foundation
import com.intellij.ui.mac.foundation.ID

internal fun notificationCenter(): ID = Foundation.invoke("NSUserNotificationCenter", "defaultUserNotificationCenter")
