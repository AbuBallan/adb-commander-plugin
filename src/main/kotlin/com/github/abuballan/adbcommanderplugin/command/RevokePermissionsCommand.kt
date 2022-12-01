package com.github.abuballan.adbcommanderplugin.command

import com.android.ddmlib.IDevice
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiver
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiverResult
import com.github.abuballan.adbcommanderplugin.extension.execute
import com.github.abuballan.adbcommanderplugin.extension.getDeviceName
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.github.abuballan.adbcommanderplugin.model.PermissionInfo
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.first
import org.jetbrains.android.facet.AndroidFacet

class RevokePermissionsCommand(private val adbCommandPrams: AdbCommandPrams) :
    AdbCommand<Map<String, Throwable?>>(adbCommandPrams) {

    private val shellReceiver = object : ShellReceiver<Unit>() {
        override fun getResult(output: String): ShellReceiverResult<Unit> = ShellReceiverResult.Success(Unit)
    }

    override fun dependsOn() = GetPermissionsCommand(adbCommandPrams)

    override fun runCommand(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        dependencyResult: AdbCommandResult<*>?
    ): AdbCommandResult<Map<String, Throwable?>> {
        return when (dependencyResult) {
            is AdbCommandResult.Success -> {
                val permissionInfoList = (dependencyResult.result as List<PermissionInfo>)
                    .filter {
                        it.isGranted == true
                    }
                if (permissionInfoList.isEmpty())
                    AdbCommandResult.Error(
                        dependencyResult.deviceName,
                        "All permissions are revoked!"
                    )
                else
                    revokePermissions(project, device, facet, packageName, permissionInfoList)
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

    private fun revokePermissions(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        permissionInfoList: List<PermissionInfo>
    ): AdbCommandResult<Map<String, Throwable?>> {
        val map = mutableMapOf<String, Throwable?>()
        permissionInfoList.forEach { permissionInfo ->
            when (val shellReceiverResult =
                device.execute("pm revoke $packageName ${permissionInfo.permissionName}", shellReceiver)) {
                is ShellReceiverResult.Success -> map[permissionInfo.permissionName] = null
                is ShellReceiverResult.Error -> map[permissionInfo.permissionName] = shellReceiverResult.error
            }
        }
        return if (map.all { it.value != null }) {
            AdbCommandResult.Error(
                device.getDeviceName(),
                "Unknown errors: ${map.first().value?.message}"
            )
        } else {
            AdbCommandResult.Success(
                device.getDeviceName(),
                map
            )
        }
    }

    override fun handleResults(
        project: Project,
        packageId: String,
        results: List<AdbCommandResult<Map<String, Throwable?>>>
    ) {
        results.forEach { result ->
            when (result) {
                is AdbCommandResult.Success -> {
                    val success = result.result
                        .filter {
                            it.value == null
                        }
                        .map { it.key }

                    val errors = result.result
                        .filter {
                            it.value != null
                        }
                        .map { it.key }

                    if (success.isNotEmpty())
                        project.showNotification(
                            NotificationType.INFORMATION,
                            "Adb Commander",
                            String.format(
                                "Permissions <b>%s</b> revoked on %s",
                                success.toTypedArray().contentToString(),
                                result.deviceName
                            )
                        )

                    if (errors.isNotEmpty())
                        project.showNotification(
                            NotificationType.INFORMATION,
                            "Adb Commander",
                            String.format(
                                "Revoking %s failed on %s",
                                errors.toTypedArray().contentToString(),
                                result.deviceName
                            )
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