package com.github.abuballan.adbcommanderplugin.command

import com.android.ddmlib.IDevice
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiver
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiverResult
import com.github.abuballan.adbcommanderplugin.extension.execute
import com.github.abuballan.adbcommanderplugin.extension.getDeviceName
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.github.abuballan.adbcommanderplugin.model.DeepLinkInfo
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

class OpenDeepLinkCommand(
    private val deepLinkInfo: DeepLinkInfo,
    adbCommandPrams: AdbCommandPrams
) : AdbCommand<String>(adbCommandPrams) {

    private val shellReceiver = object : ShellReceiver<String>() {
        override fun getResult(output: String): ShellReceiverResult<String> = ShellReceiverResult.Success(output)
    }

    override fun runCommand(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        dependencyResult: AdbCommandResult<*>?
    ): AdbCommandResult<String> {
        val action = if (deepLinkInfo.action.isNullOrEmpty()) "" else " -a ${deepLinkInfo.action}"
        val category = if (deepLinkInfo.category.isNullOrEmpty()) "" else " -c ${deepLinkInfo.category}"
        val deepLink = if (deepLinkInfo.deepLink.isNullOrEmpty()) "" else " -d ${deepLinkInfo.deepLink}"
        return when (val shellReceiverResult =
            device.execute("am start -W$action$category$deepLink", shellReceiver)) {
            is ShellReceiverResult.Success -> {
                if (shellReceiverResult.result.contains("Status: ok")) {
                    AdbCommandResult.Success(
                        device.getDeviceName(),
                        shellReceiverResult.result
                    )
                } else {
                    AdbCommandResult.Error(
                        device.getDeviceName(),
                        shellReceiverResult.result
                    )
                }
            }

            is ShellReceiverResult.Error -> AdbCommandResult.Error(
                device.getDeviceName(),
                "Unknown error: ${shellReceiverResult.error.message ?: ""}"
            )
        }
    }

    override fun handleResults(project: Project, packageId: String, results: List<AdbCommandResult<String>>) {
        results.forEach { result ->
            when (result) {
                is AdbCommandResult.Success -> {
                    val deepLinkResult = if (deepLinkInfo.deepLink == null) deepLinkInfo.action else deepLinkInfo.deepLink
                    project.showNotification(
                        NotificationType.INFORMATION,
                        "Adb Commander",
                        "Starting <b>$deepLinkResult</b> on ${result.deviceName}"
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