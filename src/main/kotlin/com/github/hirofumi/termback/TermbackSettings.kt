package com.github.hirofumi.termback

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.util.SystemInfo

enum class NotificationDestination {
    IDE,
    SYSTEM,
    ;

    companion object {
        val default: NotificationDestination = if (SystemInfo.isMac) SYSTEM else IDE
    }
}

@Service(Service.Level.APP)
@State(name = "com.github.hirofumi.termback.TermbackSettings", storages = [Storage("termback.xml")])
class TermbackSettings : SimplePersistentStateComponent<TermbackSettings.State>(State()) {
    class State : BaseState() {
        var notificationDestination by enum(NotificationDestination.default)
        var skipPopupWhenSingleNotification by property(false)
    }

    companion object {
        fun getInstance(): TermbackSettings = ApplicationManager.getApplication().getService(TermbackSettings::class.java)
    }
}
