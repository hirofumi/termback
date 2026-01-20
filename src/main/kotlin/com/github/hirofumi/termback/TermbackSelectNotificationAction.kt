package com.github.hirofumi.termback

import com.github.hirofumi.termback.notification.TermbackNotificationHandle
import com.intellij.icons.AllIcons
import com.intellij.ide.RecentProjectIconHelper
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.ListCellRenderer
import kotlin.io.path.Path

class TermbackSelectNotificationAction : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = collectUnexpiredNotifications().isNotEmpty()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val handles = collectUnexpiredNotifications()
        if (handles.isEmpty()) return

        val skipPopup = TermbackSettings.getInstance().state.skipPopupWhenSingleNotification
        if (handles.size == 1 && skipPopup) {
            // actionPerformed may run on BGT; navigateToTab requires EDT.
            UIUtil.invokeLaterIfNeeded { navigateToNotification(handles.first()) }
            return
        }

        val project = e.getData(CommonDataKeys.PROJECT) ?: return

        val builder =
            JBPopupFactory
                .getInstance()
                .createPopupChooserBuilder(handles)
                .setTitle(TermbackBundle.message("popup.title"))
                .setRenderer(TermbackNotificationCellRenderer())
                .setItemChosenCallback { navigateToNotification(it) }
                .setRequestFocus(true)
                .setMinSize(JBUI.size(400, 200))

        val list = (builder as? PopupChooserBuilder<*>)?.chooserComponent as? JList<*>
        if (list != null) {
            builder.registerKeyboardAction(KeyStroke.getKeyStroke('j')) {
                val newIndex = (list.selectedIndex + 1) % list.model.size
                list.selectedIndex = newIndex
                list.ensureIndexIsVisible(newIndex)
            }
            builder.registerKeyboardAction(KeyStroke.getKeyStroke('k')) {
                val newIndex = (list.selectedIndex - 1 + list.model.size) % list.model.size
                list.selectedIndex = newIndex
                list.ensureIndexIsVisible(newIndex)
            }
        }

        builder.createPopup().showCenteredInCurrentWindow(project)
    }

    private fun collectUnexpiredNotifications(): List<TermbackNotificationHandle> =
        TermbackSessionRegistry
            .getInstance()
            .getAllSessions()
            .flatMap { it.getUnexpiredNotifications() }

    private fun navigateToNotification(handle: TermbackNotificationHandle) {
        handle.notification.session.navigateToTab()
    }
}

private class TermbackNotificationCellRenderer : ListCellRenderer<TermbackNotificationHandle> {
    private val panel = JPanel(BorderLayout(JBUI.scale(8), 0))
    private val iconLabel = JBLabel()
    private val textPanel = JPanel(BorderLayout())
    private val titleLabel = JBLabel()
    private val messageLabel = JBLabel()
    private val iconHelper = RecentProjectIconHelper()

    init {
        // Match notification balloon style: bold title, regular message
        titleLabel.font = UIUtil.getLabelFont().deriveFont(java.awt.Font.BOLD)
        messageLabel.font = UIUtil.getLabelFont()
        textPanel.add(titleLabel, BorderLayout.NORTH)
        textPanel.add(messageLabel, BorderLayout.SOUTH)
        textPanel.isOpaque = false
        panel.add(iconLabel, BorderLayout.WEST)
        panel.add(textPanel, BorderLayout.CENTER)
        panel.border = JBUI.Borders.empty(4, 8)
        panel.isOpaque = true
    }

    override fun getListCellRendererComponent(
        list: JList<out TermbackNotificationHandle>,
        value: TermbackNotificationHandle,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
    ): Component {
        val project = value.notification.session.project
        val projectPath = project.basePath

        iconLabel.icon =
            if (projectPath != null) {
                iconHelper.getProjectIcon(Path(projectPath), isProjectValid = true)
            } else {
                AllIcons.Nodes.IdeaProject
            }

        titleLabel.text = "[${value.notification.session.project.name}] ${value.notification.title}"
        messageLabel.text = value.notification.message.ifEmpty { null }

        if (isSelected) {
            panel.background = list.selectionBackground
            titleLabel.foreground = list.selectionForeground
            messageLabel.foreground = list.selectionForeground
        } else {
            panel.background = list.background
            titleLabel.foreground = list.foreground
            messageLabel.foreground = list.foreground
        }

        return panel
    }
}
