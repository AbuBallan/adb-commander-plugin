package com.github.abuballan.adbcommanderplugin.command

import com.android.ddmlib.IDevice
import com.github.abuballan.adbcommanderplugin.command.OpenCurrentActivityCommand.ActivityModel
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiver
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiverResult
import com.github.abuballan.adbcommanderplugin.extension.execute
import com.github.abuballan.adbcommanderplugin.extension.getDeviceName
import com.github.abuballan.adbcommanderplugin.extension.openClassByName
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.github.abuballan.adbcommanderplugin.ui.selectclass.model.SelectClassExtra
import com.github.abuballan.adbcommanderplugin.ui.selectclass.openSelectActivityPopup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

class OpenCurrentActivityCommand(adbCommandPrams: AdbCommandPrams) : AdbCommand<List<ActivityModel>>(adbCommandPrams) {

    private val shellReceiver = object : ShellReceiver<List<ActivityModel>>() {
        override fun getResult(output: String): ShellReceiverResult<List<ActivityModel>> {
            val result = output.split(" ")
                .filter { it.contains("/") }
                .map { it.replace("/.", ".") }
                .map { it.replace(Regex(".+/"), "") }
                .flatMap { activityName ->
                    if (activityName.isBlank()) listOf() else listOf(activityName)
                }
                .map { ActivityModel(it) }
            return ShellReceiverResult.Success(result)
        }
    }

    override fun runCommand(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        dependencyResult: AdbCommandResult<*>?
    ): AdbCommandResult<List<ActivityModel>> {
        return when (val shellReceiverResult =
            device.execute("dumpsys activity activities | grep mResumedActivity", shellReceiver)) {
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
        results: List<AdbCommandResult<List<ActivityModel>>>
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
                "Unknown Error..."
            )
        else {
            val selectClassExtras = mutableListOf<SelectClassExtra>()
            results.forEach { result ->
                if (result is AdbCommandResult.Success) {
                    selectClassExtras.add(SelectClassExtra.DeviceName(result.deviceName))
                    result.result.forEach { activityModel ->
                        selectClassExtras.add(SelectClassExtra.ClassName(activityModel.activityName))
                    }
                }
            }
            project.openSelectActivityPopup(
                "Select an Activity",
                selectClassExtras
            ) { activityName ->
                project.openClassByName(activityName)
            }
        }
    }

    data class ActivityModel(
        val activityName: String
    )

}