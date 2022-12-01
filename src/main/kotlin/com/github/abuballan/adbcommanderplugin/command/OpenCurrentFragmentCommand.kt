package com.github.abuballan.adbcommanderplugin.command

import com.android.ddmlib.IDevice
import com.github.abuballan.adbcommanderplugin.command.OpenCurrentFragmentCommand.FragmentModel
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiver
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiverResult
import com.github.abuballan.adbcommanderplugin.extension.*
import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.github.abuballan.adbcommanderplugin.model.LineIndexed
import com.github.abuballan.adbcommanderplugin.ui.selectclass.model.SelectClassExtra
import com.github.abuballan.adbcommanderplugin.ui.selectclass.openSelectActivityPopup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

class OpenCurrentFragmentCommand(adbCommandPrams: AdbCommandPrams) : AdbCommand<List<FragmentModel>>(adbCommandPrams) {

    private val shellReceiver = object : ShellReceiver<List<FragmentModel>>() {
        override fun getResult(output: String): ShellReceiverResult<List<FragmentModel>> {
            val trimLines = output
                .lines()
                .mapIndexed { index, line ->
                    LineIndexed(index, line.trim())
                }

            val result = trimLines
                .filter {
                    it.line.contains("Added Fragments:") || it.line.contains("Active Fragments:") || it.line.contains("mFragmentId=")
                }
                .flatMap {
                    if (it.line.contains("Added Fragments:") || it.line.contains("Active Fragments:")) {
                        var index = it.index + 1
                        var line: String = trimLines[index].line
                        val list = mutableListOf<String>()
                        while (line == "null" || (line.contains("{") && line.contains("}"))) {
                            if (line != "null") list.add(line.substringAfter(": ").substringBefore("{"))
                            index += 1
                            line = trimLines[index].line
                        }
                        list
                    } else {
                        var index = it.index - 1
                        var line: String = trimLines[index].line
                        while (line == "null") {
                            index -= 1
                            line = trimLines[index].line
                        }
                        listOf(line.substringAfter(": ").substringBefore("{"))
                    }
                }
                .distinct()
                .filter { fragmentName ->
                    adbCommandPrams.project.getPsiClassByShortName(fragmentName).any { it.isWritable }
                }
                .map {
                    FragmentModel(it)
                }

            return ShellReceiverResult.Success(result)
        }
    }

    override fun runCommand(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        dependencyResult: AdbCommandResult<*>?
    ): AdbCommandResult<List<FragmentModel>> {
        return when (val shellReceiverResult =
            device.execute("dumpsys activity $packageName", shellReceiver)) {
            is ShellReceiverResult.Success -> AdbCommandResult.Success(
                device.getDeviceName(),
                shellReceiverResult.result
            )

            is ShellReceiverResult.Error -> AdbCommandResult.Error(
                device.getDeviceName(),
                "Unknown error: ${shellReceiverResult.error.message ?: ""}"
            )
        }
    }

    override fun handleResults(
        project: Project,
        packageId: String,
        results: List<AdbCommandResult<List<FragmentModel>>>
    ) {
        if (results.all { it is AdbCommandResult.Error })
            results.forEach {
                it as AdbCommandResult.Error
                project.showNotification(
                    NotificationType.ERROR,
                    "Adb Commander",
                    "${it.deviceName}: ${it.errorMsg}"
                )
            }
        else if (results.all { it is AdbCommandResult.Success && it.result.isEmpty() })
            project.showNotification(
                NotificationType.ERROR,
                "Adb Commander",
                "Check <b>$packageId</b> if opened on your device!"
            )
        else {
            val selectClassExtras = mutableListOf<SelectClassExtra>()
            results.forEach { result ->
                if (result is AdbCommandResult.Success) {
                    selectClassExtras.add(SelectClassExtra.DeviceName(result.deviceName))
                    result.result.forEach { fragmentModel ->
                        selectClassExtras.add(SelectClassExtra.ClassName(fragmentModel.fragmentName))
                    }
                }
            }
            project.openSelectActivityPopup(
                "Select a Fragment",
                selectClassExtras
            ) { activityName ->
                project.openClassByShortName(activityName)
            }
        }
    }

    data class FragmentModel(
        val fragmentName: String
    )

}