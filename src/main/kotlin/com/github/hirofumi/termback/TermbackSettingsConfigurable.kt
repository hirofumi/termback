package com.github.hirofumi.termback

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer

class TermbackSettingsConfigurable : BoundConfigurable("Termback") {
    override fun createPanel() =
        panel {
            row(TermbackBundle.message("settings.notificationDestination")) {
                comboBox(
                    items = NotificationDestination.entries,
                    renderer =
                        textListCellRenderer { value ->
                            when (value) {
                                NotificationDestination.IDE -> {
                                    TermbackBundle.message("settings.notificationDestination.ide")
                                }

                                NotificationDestination.SYSTEM -> {
                                    TermbackBundle.message(
                                        if (SystemInfo.isMac) {
                                            "settings.notificationDestination.system.macOs"
                                        } else {
                                            "settings.notificationDestination.system.other"
                                        },
                                    )
                                }

                                null -> {
                                    ""
                                }
                            }
                        },
                ).bindItem(TermbackSettings.getInstance().state::notificationDestination.toNullableProperty())
            }
            row {
                checkBox(TermbackBundle.message("settings.skipPopupWhenSingle"))
                    .bindSelected(TermbackSettings.getInstance().state::skipPopupWhenSingleNotification)
            }
        }
}
