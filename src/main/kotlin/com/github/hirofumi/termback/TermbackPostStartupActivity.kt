package com.github.hirofumi.termback

import com.github.hirofumi.termback.notification.TermbackNotifier
import com.github.hirofumi.termback.session.TermbackSession
import com.github.hirofumi.termback.session.TermbackSessionId
import com.github.hirofumi.termback.session.TermbackSessionRegistry
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.Disposer
import com.intellij.terminal.frontend.toolwindow.TerminalTabsManagerListener
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTab
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTabsManager
import kotlinx.coroutines.launch

private val LOG = logger<TermbackPostStartupActivity>()

class TermbackPostStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val registry = TermbackSessionRegistry.getInstance()
        val notifier = TermbackNotifier.getInstance()
        val listener = Listener(project, registry, notifier)

        project.messageBus.connect(project).subscribe(TerminalTabsManagerListener.TOPIC, listener)
        TerminalToolWindowTabsManager.getInstance(project).tabs.forEach(listener::tabAdded)
    }

    private class Listener(
        private val project: Project,
        private val registry: TermbackSessionRegistry,
        private val notifier: TermbackNotifier,
    ) : TerminalTabsManagerListener {
        override fun tabAdded(tab: TerminalToolWindowTab) {
            tab.view.coroutineScope.launch {
                val startupOptions =
                    runCatching { tab.view.startupOptionsDeferred.await() }.getOrElse {
                        LOG.warn("termback failed to await startup options for '${tab.content.displayName}'", it)
                        return@launch
                    }
                val sessionIdValue = startupOptions.envVariables["TERMBACK_SESSION_ID"] ?: return@launch

                val session =
                    TermbackSession(
                        id = TermbackSessionId(sessionIdValue),
                        project = project,
                        content = tab.content,
                    )

                if (!registry.register(session)) {
                    return@launch
                }
                Disposer.register(tab.content) {
                    cleanup(session)
                }
            }
        }

        private fun cleanup(session: TermbackSession) {
            registry.unregister(session)
            notifier.expireSessionNotifications(session)
        }
    }
}
