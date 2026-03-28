package com.github.hirofumi.termback

import com.intellij.openapi.project.Project
import com.intellij.platform.eel.EelDescriptor
import org.jetbrains.plugins.terminal.LocalTerminalCustomizer

class TermbackLocalTerminalCustomizer : LocalTerminalCustomizer() {
    override fun customizeCommandAndEnvironment(
        project: Project,
        workingDirectory: String?,
        shellCommand: MutableList<String>,
        envs: MutableMap<String, String>,
        eelDescriptor: EelDescriptor,
    ): MutableList<String> {
        envs["TERMBACK_ENDPOINT"] = TermbackRestService.getEndpointUrl()
        envs["TERMBACK_SESSION_ID"] = TermbackSessionId.generate().value
        return shellCommand
    }
}
