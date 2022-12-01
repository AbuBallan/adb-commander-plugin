package com.github.abuballan.adbcommanderplugin.action

import com.github.abuballan.adbcommanderplugin.command.RevokePermissionsCommand
import com.github.abuballan.adbcommanderplugin.extension.getSelectedAdbCommandParams
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

class RevokePermissionsAction : AdbAction() {

    override fun actionPerformed(event: AnActionEvent, project: Project) {
        runCatching {
            project.getSelectedAdbCommandParams()
        }
            .onSuccess { selectedAdbCommandParams ->
                RevokePermissionsCommand(selectedAdbCommandParams).execute {}
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