package com.github.abuballan.adbcommanderplugin.action

import com.github.abuballan.adbcommanderplugin.command.OpenDeepLinkCommand
import com.github.abuballan.adbcommanderplugin.extension.getSelectedAdbCommandParams
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.github.abuballan.adbcommanderplugin.services.DeepLinksInfoPersistentService
import com.github.abuballan.adbcommanderplugin.services.VariableCollectionPersistentService
import com.github.abuballan.adbcommanderplugin.ui.deeplinks.DeeplinksDialog
import com.github.abuballan.adbcommanderplugin.ui.utils.substituteVariables
import com.intellij.ide.actions.QuickSwitchSchemeAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project

class OpenDeeplinksAction : QuickSwitchSchemeAction() {

    override fun fillActions(project: Project?, group: DefaultActionGroup, dataContext: DataContext) {
        val items = DeepLinksInfoPersistentService.getInstance(project!!).deepLinksInfoListState.items
        val variableCollection = VariableCollectionPersistentService.getInstance(project).collection.items

        for (item in items) {
            group.add(object : AnAction(item.name) {
                override fun actionPerformed(e: AnActionEvent) {
                    val keyValueArray = variableCollection.find { it.isActive == true }?.variableItems ?: emptyList()
                    runCatching {
                        project.getSelectedAdbCommandParams()
                    }
                        .onSuccess { selectedAdbCommandParams ->
                            OpenDeepLinkCommand(
                                substituteVariables(item, keyValueArray),
                                selectedAdbCommandParams
                            ).execute {}
                        }
                        .onFailure { error ->
                            project.showNotification(
                                NotificationType.ERROR,
                                "ADB Commander",
                                error.message ?: ""
                            )
                        }
                }
            })
        }
        group.add(Separator())
        group.add(object : AnAction("DeepLink Editor") {
            override fun actionPerformed(event: AnActionEvent) {
                DeeplinksDialog(event.project!!).show()
            }
        })
    }
}