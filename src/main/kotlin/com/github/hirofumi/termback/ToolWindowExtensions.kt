package com.github.hirofumi.termback

import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content
import com.intellij.util.concurrency.annotations.RequiresEdt
import javax.swing.SwingUtilities

@RequiresEdt
fun ToolWindow.getTabState(content: Content): TermbackTabState {
    val isSelected = content.manager?.selectedContent === content
    if (!isSelected) return TermbackTabState.NOT_VISIBLE
    if (!isVisible) return TermbackTabState.NOT_VISIBLE
    if (!isActive) return TermbackTabState.VISIBLE_INACTIVE

    val isSplit = content.manager !== contentManager
    if (!isSplit) return TermbackTabState.VISIBLE_ACTIVE

    val focusOwner = IdeFocusManager.getGlobalInstance().focusOwner ?: return TermbackTabState.VISIBLE_INACTIVE
    val hasFocus = SwingUtilities.isDescendingFrom(focusOwner, content.component)
    return if (hasFocus) TermbackTabState.VISIBLE_ACTIVE else TermbackTabState.VISIBLE_INACTIVE
}
