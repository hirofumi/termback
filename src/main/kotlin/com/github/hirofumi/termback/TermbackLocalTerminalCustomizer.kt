package com.github.hirofumi.termback

import com.github.hirofumi.termback.notification.TermbackNotifier
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Disposer
import com.intellij.platform.eel.EelDescriptor
import com.intellij.util.ui.UIUtil
import org.jetbrains.plugins.terminal.LocalTerminalCustomizer

private val LOG = logger<TermbackLocalTerminalCustomizer>()

class TermbackLocalTerminalCustomizer : LocalTerminalCustomizer() {
    override fun customizeCommandAndEnvironment(
        project: Project,
        workingDirectory: String?,
        shellCommand: MutableList<String>,
        envs: MutableMap<String, String>,
        eelDescriptor: EelDescriptor,
    ): MutableList<String> {
        val registry = TermbackSessionRegistry.getInstance()
        val content =
            UIUtil.invokeAndWaitIfNeeded(
                Computable {
                    val toolWindow = project.getTerminalToolWindow() ?: return@Computable null
                    toolWindow.contentManager.contentsRecursively.find { content ->
                        toolWindow.getTabState(content) == TermbackTabState.VISIBLE_ACTIVE
                    } ?: toolWindow.contentManager.contentsRecursively.findLast { content ->
                        registry.findByContent(content) == null
                    }
                },
            )

        if (content == null) {
            LOG.warn("No active terminal tab found; session not registered")
        } else {
            val session = TermbackSession(project, content)

            registry.register(session)
            Disposer.register(content) {
                registry.unregister(session)
                TermbackNotifier.getInstance().expireSessionNotifications(session)
            }

            envs["TERMBACK_ENDPOINT"] = TermbackRestService.getEndpointUrl()
            envs["TERMBACK_SESSION_ID"] = session.id.value
        }

        return shellCommand
    }
}
