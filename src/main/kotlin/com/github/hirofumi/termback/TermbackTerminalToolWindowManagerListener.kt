package com.github.hirofumi.termback

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.openapi.wm.ex.ToolWindowManagerListener.ToolWindowManagerEventType
import com.intellij.openapi.wm.ex.ToolWindowManagerListener.ToolWindowManagerEventType.ActivateToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener.ToolWindowManagerEventType.ShowToolWindow
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory

class TermbackTerminalToolWindowManagerListener(
    private val project: Project,
) : ToolWindowManagerListener {
    override fun stateChanged(
        toolWindowManager: ToolWindowManager,
        toolWindow: ToolWindow,
        changeType: ToolWindowManagerEventType,
    ) {
        if (toolWindow.id != TerminalToolWindowFactory.TOOL_WINDOW_ID) return
        if (changeType != ActivateToolWindow && changeType != ShowToolWindow) return
        if (project.isDisposed) return
        TermbackNotifier.getInstance().expireSuppressedNotifications(project)
    }
}
