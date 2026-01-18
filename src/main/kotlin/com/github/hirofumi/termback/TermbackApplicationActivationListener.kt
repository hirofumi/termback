package com.github.hirofumi.termback

import com.github.hirofumi.termback.notification.TermbackNotifier
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.IdeFrame

class TermbackApplicationActivationListener : ApplicationActivationListener {
    override fun applicationActivated(ideFrame: IdeFrame) {
        for (project in ProjectManager.getInstance().availableProjects) {
            TermbackNotifier.getInstance().expireSuppressedNotifications(project)
        }
    }
}
