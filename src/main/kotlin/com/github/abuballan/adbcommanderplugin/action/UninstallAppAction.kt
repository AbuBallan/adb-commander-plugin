package com.github.abuballan.adbcommanderplugin.action

import com.github.abuballan.adbcommanderplugin.command.UninstallAppCommand
import com.github.abuballan.adbcommanderplugin.extension.getSelectedAdbCommandParams
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

class UninstallAppAction : AdbAction() {
    override fun actionPerformed(event: AnActionEvent, project: Project) {
        runCatching {
            project.getSelectedAdbCommandParams()
        }
            .onSuccess { selectedAdbCommandParams ->
                UninstallAppCommand(selectedAdbCommandParams).execute {}
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