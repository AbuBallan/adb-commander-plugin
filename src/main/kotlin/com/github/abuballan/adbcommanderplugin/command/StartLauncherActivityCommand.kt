package com.github.abuballan.adbcommanderplugin.command

import com.android.ddmlib.IDevice
import com.android.tools.idea.run.activity.ActivityLocator
import com.android.tools.idea.run.activity.DefaultActivityLocator
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiver
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiverResult
import com.github.abuballan.adbcommanderplugin.extension.execute
import com.github.abuballan.adbcommanderplugin.extension.getDeviceName
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable
import org.jetbrains.android.facet.AndroidFacet

open class StartLauncherActivityCommand(adbCommandPrams: AdbCommandPrams) : AdbCommand<Unit>(adbCommandPrams) {

    private val shellReceiver = object : ShellReceiver<Unit>() {
        override fun getResult(output: String): ShellReceiverResult<Unit> {
            return if (output.lines().size in 1..2)
                ShellReceiverResult.Success(Unit)
            else
                ShellReceiverResult.Error(RuntimeException(output))
        }
    }

    override fun runCommand(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        dependencyResult: AdbCommandResult<*>?
    ): AdbCommandResult<Unit> {
        if (dependencyResult == null)
            return startLauncherActivity(facet, device, packageName)
        else
            return when (dependencyResult) {
                is AdbCommandResult.Success -> {
                    startLauncherActivity(facet, device, packageName)
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

    private fun startLauncherActivity(
        facet: AndroidFacet,
        device: IDevice,
        packageName: String
    ): AdbCommandResult<Unit> {
        return runCatching {
            getDefaultActivityName(facet, device)
        }
            .fold(
                onSuccess = { launcherActivity ->
                    return when (val shellReceiverResult =
                        device.execute("am start -n $packageName/$launcherActivity", shellReceiver)) {
                        is ShellReceiverResult.Success -> AdbCommandResult.Success(
                            device.getDeviceName(),
                            shellReceiverResult.result
                        )

                        is ShellReceiverResult.Error -> AdbCommandResult.Error(
                            device.getDeviceName(),
                            "Unknown error: ${shellReceiverResult.error.message ?: ""}"
                        )
                    }
                },
                onFailure = { error ->
                    AdbCommandResult.Error(
                        device.getDeviceName(),
                        "Unknown error: ${error.message ?: ""}"
                    )
                }
            )
    }

    @Throws(ActivityLocator.ActivityLocatorException::class)
    private fun getDefaultActivityName(facet: AndroidFacet, device: IDevice): String {
        return ApplicationManager.getApplication()
            .runReadAction(
                ThrowableComputable<String, ActivityLocator.ActivityLocatorException?> {
                    DefaultActivityLocator(facet).getQualifiedActivityName(device)
                }
            )
    }


    override fun handleResults(project: Project, packageId: String, results: List<AdbCommandResult<Unit>>) {
        results.forEach { result ->
            when (result) {
                is AdbCommandResult.Success -> {
                    project.showNotification(
                        NotificationType.INFORMATION,
                        "Adb Commander",
                        "<b>$packageId</b> started on ${result.deviceName}"
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