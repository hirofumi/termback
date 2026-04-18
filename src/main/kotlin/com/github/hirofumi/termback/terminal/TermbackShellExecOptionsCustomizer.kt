package com.github.hirofumi.termback.terminal

import com.github.hirofumi.termback.TermbackRestService
import com.github.hirofumi.termback.session.TermbackSessionId
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.terminal.startup.MutableShellExecOptions
import org.jetbrains.plugins.terminal.startup.ShellExecOptionsCustomizer

class TermbackShellExecOptionsCustomizer : ShellExecOptionsCustomizer {
    override fun customizeExecOptions(
        project: Project,
        shellExecOptions: MutableShellExecOptions,
    ) {
        shellExecOptions.setEnvironmentVariable("TERMBACK_ENDPOINT", TermbackRestService.getEndpointUrl())
        shellExecOptions.setEnvironmentVariable("TERMBACK_SESSION_ID", TermbackSessionId.generate().value)
    }
}
