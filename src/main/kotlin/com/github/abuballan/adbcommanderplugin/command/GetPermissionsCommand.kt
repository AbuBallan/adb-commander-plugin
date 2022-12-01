package com.github.abuballan.adbcommanderplugin.command

import com.android.ddmlib.IDevice
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiver
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiverResult
import com.github.abuballan.adbcommanderplugin.extension.execute
import com.github.abuballan.adbcommanderplugin.extension.getApiVersion
import com.github.abuballan.adbcommanderplugin.extension.getDeviceName
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.github.abuballan.adbcommanderplugin.model.LineIndexed
import com.github.abuballan.adbcommanderplugin.model.PermissionInfo
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

class GetPermissionsCommand(private val adbCommandPrams: AdbCommandPrams) :
    AdbCommand<List<PermissionInfo>>(adbCommandPrams) {

    private val shellReceiver = object : ShellReceiver<List<PermissionInfo>>() {

        private val permissionMap = mutableMapOf<String, Boolean?>()

        override fun getResult(output: String): ShellReceiverResult<List<PermissionInfo>> {
            val trimLines = output
                .lines()
                .mapIndexed { index, line ->
                    LineIndexed(index, line.trim())
                }

            val requestedPermissionsIndex: Int? = trimLines.find { it.line.contains("requested permissions:") }?.index
            val installPermissionsIndex: Int? = trimLines.find { it.line.contains("install permissions:") }?.index
            val runtimePermissionsIndex: Int? = trimLines.find { it.line.contains("runtime permissions:") }?.index

            requestedPermissionsIndex?.let { findPermissions(trimLines, it) }
            installPermissionsIndex?.let { findPermissions(trimLines, it) }
            runtimePermissionsIndex?.let { findPermissions(trimLines, it) }

            return ShellReceiverResult.Success(
                permissionMap.map {
                    PermissionInfo(it.key, it.value)
                }
            )
        }

        private fun findPermissions(trimLines: List<LineIndexed>, index: Int) {
            var lineIndex = index + 1
            var line = trimLines[lineIndex].line
            while (line.contains(".permission.")) {
                val permissionName = line.substringBefore(": ")
                val granted = when {
                    line.contains("granted=false") -> false
                    line.contains("granted=true") -> true
                    else -> null
                }
                if (permissionMap.contains(permissionName)) {
                    if (permissionMap[permissionName] == null) {
                        permissionMap[permissionName] = granted
                    }
                } else {
                    permissionMap[permissionName] = granted
                }
                lineIndex += 1
                line = trimLines[lineIndex].line
            }
        }
    }

    override fun dependsOn() = CheckIfAppInstalledCommand(adbCommandPrams)

    override fun runCommand(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        dependencyResult: AdbCommandResult<*>?
    ): AdbCommandResult<List<PermissionInfo>> {
        return when (dependencyResult) {
            is AdbCommandResult.Success -> {
                val isAppInstalled = dependencyResult.result as Boolean
                if (isAppInstalled && device.getApiVersion() >= 23)
                    runCommandIfAppInstalled(project, device, facet, packageName)
                else if (!isAppInstalled)
                    AdbCommandResult.Error(
                        dependencyResult.deviceName,
                        "The app is not installed"
                    )
                else
                    AdbCommandResult.Error(
                        dependencyResult.deviceName,
                        "The device must be at least api level 23"
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
    ): AdbCommandResult<List<PermissionInfo>> {
        return when (val shellReceiverResult = device.execute("dumpsys package $packageName", shellReceiver)) {
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
        results: List<AdbCommandResult<List<PermissionInfo>>>
    ) {
        results.forEach { result ->
            when (result) {
                is AdbCommandResult.Success -> {
                    project.showNotification(
                        NotificationType.INFORMATION,
                        "Adb Commander",
                        "${result.deviceName}: ${result.result.toTypedArray().contentToString()}"
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

