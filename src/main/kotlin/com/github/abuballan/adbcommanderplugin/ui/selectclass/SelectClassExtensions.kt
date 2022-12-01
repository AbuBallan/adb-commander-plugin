package com.github.abuballan.adbcommanderplugin.ui.selectclass

import com.github.abuballan.adbcommanderplugin.ui.selectclass.model.SelectClassExtra
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory

fun Project.openSelectActivityPopup(title: String, list: List<SelectClassExtra>, onSelectClass: (String) -> Unit) {
    JBPopupFactory.getInstance()
        .createListPopup(SelectClassListPopupStep(this, title, list, onSelectClass))
        .showCenteredInCurrentWindow(this)
}