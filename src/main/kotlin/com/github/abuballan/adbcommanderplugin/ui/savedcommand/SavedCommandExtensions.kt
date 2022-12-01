package com.github.abuballan.adbcommanderplugin.ui.savedcommand

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import javax.swing.JTable

fun Project.openSavedCommandPopup() {

    val popupChooserBuilder = JBPopupFactory.getInstance()
        .createPopupChooserBuilder(JTable())
}