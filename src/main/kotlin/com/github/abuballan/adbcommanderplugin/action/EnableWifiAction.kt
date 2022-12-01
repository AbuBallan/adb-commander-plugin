package com.github.abuballan.adbcommanderplugin.action

import com.github.abuballan.adbcommanderplugin.command.ToggleSvcCommand
import com.github.abuballan.adbcommanderplugin.extension.getSelectedAdbCommandParams
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.github.abuballan.adbcommanderplugin.model.SvcType
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

class EnableWifiAction : AdbAction() {
    override fun actionPerformed(event: AnActionEvent, project: Project) {
        runCatching {
            project.getSelectedAdbCommandParams()
        }
            .onSuccess { selectedAdbCommandParams ->
                ToggleSvcCommand(SvcType.WIFI, true, selectedAdbCommandParams).execute {}
            }
            .onFailure { error ->
                project.showNotification(
                    NotificationType.ERROR,
                    "ADB Commander",
                    error.message ?: ""
                )
            }
    }
}