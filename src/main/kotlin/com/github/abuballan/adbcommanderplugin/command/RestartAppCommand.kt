package com.github.abuballan.adbcommanderplugin.command

import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.intellij.openapi.project.Project

class RestartAppCommand(private val showResultsIfSuccess: Boolean, private val adbCommandPrams: AdbCommandPrams) : StartLauncherActivityCommand(adbCommandPrams) {
    override fun dependsOn() = KillApplicationCommand(adbCommandPrams)

    override fun handleResults(project: Project, packageId: String, results: List<AdbCommandResult<Unit>>) {
        if (!showResultsIfSuccess && results.all { it is AdbCommandResult.Success }) return
        super.handleResults(project, packageId, results)
    }
}