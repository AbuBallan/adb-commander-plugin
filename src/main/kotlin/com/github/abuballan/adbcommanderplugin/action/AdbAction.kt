package com.github.abuballan.adbcommanderplugin.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project

abstract class AdbAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) = actionPerformed(e, e.getData(PlatformDataKeys.PROJECT)!!)
    abstract fun actionPerformed(event: AnActionEvent, project: Project)
}