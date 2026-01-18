package com.github.hirofumi.termback.notification.channels.macos

import com.github.hirofumi.termback.TermbackSessionId
import com.github.hirofumi.termback.TermbackSessionRegistry
import com.github.hirofumi.termback.notification.TermbackNotification
import com.github.hirofumi.termback.notification.TermbackNotificationHandle
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.mac.foundation.Foundation
import com.intellij.ui.mac.foundation.ID
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.sun.jna.Callback

internal class TermbackMacOsNotificationHandle private constructor(
    override val notification: TermbackNotification,
) : TermbackNotificationHandle {
    override fun isExpired(): Boolean {
        var expired = true
        Foundation.executeOnMainThread(true, true) {
            expired = findDeliveredNotification() == null
        }
        return expired
    }

    override fun expire() {
        Foundation.executeOnMainThread(true, true) {
            findDeliveredNotification()?.let { nsUserNotification ->
                Foundation.invoke(notificationCenter(), "removeDeliveredNotification:", nsUserNotification)
            }
        }
    }

    private fun findDeliveredNotification(): ID? {
        val delivered = Foundation.invoke(notificationCenter(), "deliveredNotifications")
        val count = Foundation.invoke(delivered, "count").toInt()
        for (i in 0 until count) {
            val nsUserNotification = Foundation.invoke(delivered, "objectAtIndex:", i)
            if (SessionUserInfo.extractSessionId(nsUserNotification) == notification.session.id) return nsUserNotification
        }
        return null
    }

    companion object {
        @RequiresEdt
        fun postToSystem(notification: TermbackNotification): TermbackMacOsNotificationHandle {
            Delegate.ensureInstalled()

            Foundation.executeOnMainThread(true, true) {
                val nsUserNotification = Foundation.invoke(Foundation.invoke("NSUserNotification", "alloc"), "init")
                Foundation.invoke(nsUserNotification, "setTitle:", Foundation.nsString(notification.displayTitle))
                Foundation.invoke(nsUserNotification, "setInformativeText:", Foundation.nsString(notification.message))
                Foundation.invoke(nsUserNotification, "setUserInfo:", SessionUserInfo.create(notification.session.id))
                Foundation.invoke(notificationCenter(), "deliverNotification:", nsUserNotification)
                Foundation.cfRelease(nsUserNotification)
            }

            return TermbackMacOsNotificationHandle(notification)
        }
    }
}

private object Delegate {
    private const val CLASS_NAME = "TermbackNotificationCenterDelegate"

    @Volatile
    private var instance: ID = ID.NIL

    @Synchronized
    fun ensureInstalled() {
        if (instance != ID.NIL) return

        Foundation.executeOnMainThread(true, true) {
            instance = newInstance()
            Foundation.invoke(notificationCenter(), "setDelegate:", instance)
        }
    }

    private fun newInstance(): ID {
        val delegateClass =
            Foundation.getObjcClass(CLASS_NAME).takeIf { it != ID.NIL }
                ?: registerClass()

        return Foundation.invoke(Foundation.invoke(delegateClass, "alloc"), "init")
    }

    private fun registerClass(): ID {
        val delegateClass =
            Foundation.allocateObjcClassPair(
                Foundation.getObjcClass("NSObject"),
                CLASS_NAME,
            )

        Foundation.addMethod(
            delegateClass,
            Foundation.createSelector("userNotificationCenter:didActivateNotification:"),
            NavigateToTab,
            "v@:@@",
        )

        Foundation.addMethod(
            delegateClass,
            Foundation.createSelector("userNotificationCenter:shouldPresentNotification:"),
            AlwaysPresent,
            "c@:@@", // "c" = signed char (Objective-C BOOL), not "B" (C99 _Bool)
        )

        Foundation.registerObjcClassPair(delegateClass)

        return delegateClass
    }
}

private object SessionUserInfo {
    private const val SESSION_ID_KEY = "termbackSessionId"

    fun create(sessionId: TermbackSessionId): ID {
        val key = Foundation.nsString(SESSION_ID_KEY)
        val value = Foundation.nsString(sessionId.value)
        return Foundation.invoke(
            "NSDictionary",
            "dictionaryWithObject:forKey:",
            value,
            key,
        )
    }

    fun extractSessionId(nsUserNotification: ID): TermbackSessionId? {
        val userInfo = Foundation.invoke(nsUserNotification, "userInfo")
        val key = Foundation.nsString(SESSION_ID_KEY)
        val value = Foundation.invoke(userInfo, "objectForKey:", key).takeIf { it != ID.NIL } ?: return null
        val str = Foundation.toStringViaUTF8(value) ?: return null
        return TermbackSessionId(str)
    }
}

private object NavigateToTab : Callback {
    @Suppress("unused")
    fun callback(
        self: ID,
        selector: ID,
        center: ID,
        notification: ID,
    ) {
        val sessionId = SessionUserInfo.extractSessionId(notification) ?: return

        ApplicationManager.getApplication().invokeLater {
            val session = TermbackSessionRegistry.getInstance().findById(sessionId) ?: return@invokeLater
            session.navigateToTab()
        }
    }
}

private object AlwaysPresent : Callback {
    @Suppress("unused")
    fun callback(
        self: ID,
        selector: ID,
        center: ID,
        notification: ID,
    ): Byte = 1
}
