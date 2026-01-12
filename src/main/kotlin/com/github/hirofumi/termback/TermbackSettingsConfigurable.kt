package com.github.hirofumi.termback

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

class TermbackSettingsConfigurable : BoundConfigurable("Termback") {
    override fun createPanel() =
        panel {
            row {
                checkBox(TermbackBundle.message("settings.skipPopupWhenSingle"))
                    .bindSelected(TermbackSettings.getInstance().state::skipPopupWhenSingleNotification)
            }
        }
}
