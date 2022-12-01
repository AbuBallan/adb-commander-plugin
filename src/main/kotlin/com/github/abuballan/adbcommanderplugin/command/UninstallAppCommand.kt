package com.github.abuballan.adbcommanderplugin.command

import com.android.ddmlib.IDevice
import com.github.abuballan.adbcommanderplugin.extension.getDeviceName
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

class UninstallAppCommand(adbCommandPrams: AdbCommandPrams) : AdbCommand<Unit>(adbCommandPrams) {

    override fun runCommand(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        dependencyResult: AdbCommandResult<*>?
    ): AdbCommandResult<Unit> {
        return runCatching {
            device.uninstallPackage(packageName)
        }.fold(onSuccess = { errorCode ->
                errorCode?.let {
                    AdbCommandResult.Error(device.getDeviceName(), "The app in not installed on your device")
                } ?: AdbCommandResult.Success(device.getDeviceName(), Unit)
            }, onFailure = { error ->
                AdbCommandResult.Error(device.getDeviceName(), "Unknown Error: ${error.message ?: ""}")
            })
    }

    override fun handleResults(project: Project, packageId: String, results: List<AdbCommandResult<Unit>>) {
        results.forEach { result ->
            when (result) {
                is AdbCommandResult.Success -> {
                    project.showNotification(
                        NotificationType.INFORMATION,
                        "Adb Commander",
                        "<b>$packageId</b> uninstalled on ${result.deviceName}"
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