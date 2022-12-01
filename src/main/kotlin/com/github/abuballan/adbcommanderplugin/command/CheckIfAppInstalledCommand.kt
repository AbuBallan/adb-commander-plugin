package com.github.abuballan.adbcommanderplugin.command

import com.android.ddmlib.IDevice
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiver
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiverResult
import com.github.abuballan.adbcommanderplugin.extension.execute
import com.github.abuballan.adbcommanderplugin.extension.getDeviceName
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

class CheckIfAppInstalledCommand(
    adbCommandPrams: AdbCommandPrams
) : AdbCommand<Boolean>(adbCommandPrams) {

    private val shellReceiver = object : ShellReceiver<Boolean>() {
        override fun getResult(output: String): ShellReceiverResult<Boolean> {
            return if (output.isBlank())
                ShellReceiverResult.Success(false)
            else
                ShellReceiverResult.Success(true)
        }
    }

    override fun runCommand(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        dependencyResult: AdbCommandResult<*>?
    ): AdbCommandResult<Boolean> {
        return when (val shellReceiverResult = device.execute("pm list packages $packageName", shellReceiver)) {
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

    override fun handleResults(project: Project, packageId: String, results: List<AdbCommandResult<Boolean>>) {
        results.forEach { result ->
            when {
                result is AdbCommandResult.Success && result.result -> {
                    project.showNotification(
                        NotificationType.INFORMATION,
                        "Adb Commander",
                        "The app is installed on ${result.deviceName}"
                    )
                }
                result is AdbCommandResult.Success && !result.result -> {
                    project.showNotification(
                        NotificationType.INFORMATION,
                        "Adb Commander",
                        "The app is not installed on ${result.deviceName}"
                    )
                }
                result is AdbCommandResult.Error -> {
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