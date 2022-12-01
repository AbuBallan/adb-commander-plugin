package com.github.abuballan.adbcommanderplugin.action

import com.github.abuballan.adbcommanderplugin.command.ClearAppDataCommand
import com.github.abuballan.adbcommanderplugin.command.RestartAppCommand
import com.github.abuballan.adbcommanderplugin.extension.getSelectedAdbCommandParams
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

class ClearAppDataAndRestartAction : AdbAction() {
    override fun actionPerformed(event: AnActionEvent, project: Project) {
        runCatching {
            project.getSelectedAdbCommandParams()
        }
            .onSuccess { selectedAdbCommandParams ->
                ClearAppDataCommand(selectedAdbCommandParams).execute {
                    RestartAppCommand(false, selectedAdbCommandParams).execute{}
                }
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