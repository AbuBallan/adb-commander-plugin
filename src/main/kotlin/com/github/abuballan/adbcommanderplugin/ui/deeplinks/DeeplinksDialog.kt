package com.github.abuballan.adbcommanderplugin.ui.deeplinks

import com.github.abuballan.adbcommanderplugin.model.DeepLinkInfo
import com.github.abuballan.adbcommanderplugin.model.VariableCollection
import com.github.abuballan.adbcommanderplugin.services.DeepLinksInfoPersistentService
import com.github.abuballan.adbcommanderplugin.services.VariableCollectionPersistentService
import com.google.gson.Gson
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTabbedPane
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JComponent

class DeeplinksDialog(private val project: Project) : DialogWrapper(project) {

    private val gson = Gson()

    private val persistentDeepLinks
        get() = DeepLinksInfoPersistentService.getInstance(project).deepLinksInfoListState

    private val persistentVariable
        get() = VariableCollectionPersistentService.getInstance(project).collection

    // Deep copy
    private val modifiableDeepLinksInfo = ArrayList<DeepLinkInfo>().apply {
        persistentDeepLinks.items.map { item -> add(gson.fromJson(gson.toJson(item), DeepLinkInfo::class.java)) }
    }

    // Deep copy
    private val modifiableVariable = ArrayList<VariableCollection>().apply {
        persistentVariable.items.map { item -> add(gson.fromJson(gson.toJson(item), VariableCollection::class.java)) }
    }

    private val applyAction = object : AbstractAction("Apply") {
        override fun actionPerformed(e: ActionEvent?) {
            save()
        }
    }

    private val variablesTab = VariablesTab(modifiableVariable, gson)
    private val deepLinksTab = DeeplinksTap(project, modifiableDeepLinksInfo) {
        return@DeeplinksTap variablesTab.getActive()
    }.get()

    init {
        init()
        title = "DeepLinks Editor"
        getButton(okAction)?.text = "Save"
        getButton(cancelAction)?.text = "Close"
    }

    override fun createCenterPanel(): JComponent {
        return JBTabbedPane().apply {
            insertTab(
                "DeepLinks",
                AllIcons.Ide.UpDown,
                deepLinksTab,
                "Collection of DeepLinks",
                0,
            )

            insertTab(
                "Variables",
                AllIcons.General.InlineVariablesHover,
                variablesTab.get(),
                "Collection of environment variables",
                1
            )
            minimumSize = Dimension(700, 700)
        }
    }

    override fun createActions() = arrayOf(cancelAction, okAction, applyAction, helpAction)

    override fun doOKAction() {
        save()
        super.doOKAction()
    }

    private fun save() {
        persistentDeepLinks.items.clear()
        persistentVariable.items.clear()

        persistentDeepLinks.items.addAll(modifiableDeepLinksInfo.map { it.copy() })
        persistentVariable.items.addAll(modifiableVariable.map { it.copy() })
    }


}