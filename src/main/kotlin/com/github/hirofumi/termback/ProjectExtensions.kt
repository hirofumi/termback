package com.github.hirofumi.termback

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory

/**
 * Returns the Terminal tool window for this project, or null if not available.
 */
fun Project.getTerminalToolWindow(): ToolWindow? =
    ToolWindowManager.getInstance(this).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID)
