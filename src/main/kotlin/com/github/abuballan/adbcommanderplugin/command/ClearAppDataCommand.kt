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

class ClearAppDataCommand (private val adbCommandPrams: AdbCommandPrams) : AdbCommand<Unit>(adbCommandPrams) {

    private val shellReceiver = object : ShellReceiver<Unit>() {
        override fun getResult(output: String): ShellReceiverResult<Unit> = ShellReceiverResult.Success(Unit)
    }


    override fun dependsOn() = CheckIfAppInstalledCommand(adbCommandPrams)

    override fun runCommand(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        dependencyResult: AdbCommandResult<*>?
    ): AdbCommandResult<Unit> {
        return when (dependencyResult) {
            is AdbCommandResult.Success -> {
                val isAppInstalled = dependencyResult.result as Boolean
                if (isAppInstalled)
                    runCommandIfAppInstalled(project, device, facet, packageName)
                else
                    AdbCommandResult.Error(
                        dependencyResult.deviceName,
                        "The app is not installed"
                    )
            }

            is AdbCommandResult.Error -> {
                AdbCommandResult.Error(
                    dependencyResult.deviceName,
                    dependencyResult.errorMsg
                )
            }

            else -> {
                AdbCommandResult.Error(
                    device.getDeviceName(),
                    "Unknown error"
                )
            }
        }
    }

    private fun runCommandIfAppInstalled(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String
    ): AdbCommandResult<Unit> {
        return when (val shellReceiverResult = device.execute("pm clear $packageName", shellReceiver)) {
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

    override fun handleResults(project: Project, packageId: String, results: List<AdbCommandResult<Unit>>) {
        results.forEach { result ->
            when (result) {
                is AdbCommandResult.Success -> {
                    project.showNotification(
                        NotificationType.INFORMATION,
                        "Adb Commander",
                        "<b>$packageId</b> cleared data for app on ${result.deviceName}"
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