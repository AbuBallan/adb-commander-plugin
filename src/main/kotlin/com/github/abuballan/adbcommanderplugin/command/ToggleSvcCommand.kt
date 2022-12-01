package com.github.abuballan.adbcommanderplugin.command

import com.android.ddmlib.IDevice
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiver
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiverResult
import com.github.abuballan.adbcommanderplugin.extension.execute
import com.github.abuballan.adbcommanderplugin.extension.getDeviceName
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.github.abuballan.adbcommanderplugin.model.SvcType
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

class ToggleSvcCommand(private val svcType: SvcType, private val isEnabled: Boolean, adbCommandPrams: AdbCommandPrams) :
    AdbCommand<Unit>(adbCommandPrams) {

    private val shellReceiver = object : ShellReceiver<Unit>() {
        override fun getResult(output: String): ShellReceiverResult<Unit> = ShellReceiverResult.Success(Unit)
    }

    override fun runCommand(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        dependencyResult: AdbCommandResult<*>?
    ): AdbCommandResult<Unit> {
        return when (val shellReceiverResult =
            device.execute("svc ${svcType.parameter} ${if (isEnabled) "enable" else "disable"}", shellReceiver)) {
            is ShellReceiverResult.Success -> AdbCommandResult.Success(
                device.getDeviceName(), shellReceiverResult.result
            )

            is ShellReceiverResult.Error -> AdbCommandResult.Error(
                device.getDeviceName(), "Unknown error: ${shellReceiverResult.error.message ?: ""}"
            )
        }
    }

    override fun handleResults(project: Project, packageId: String, results: List<AdbCommandResult<Unit>>) {
        results.forEach { result ->
            when (result) {
                is AdbCommandResult.Success -> {
                    project.showNotification(
                        NotificationType.INFORMATION,
                        "Adb Commander",
                        "<b>${svcType.description}</b> ${if (isEnabled) "enabled" else "disabled"} on ${result.deviceName}"
                    )
                }

                is AdbCommandResult.Error -> {
                    project.showNotification(
                        NotificationType.ERROR,
                        "Adb Commander",
                        "${result.deviceName}: ${result.errorMsg}"
                    )
                }
            }
        }
    }

}