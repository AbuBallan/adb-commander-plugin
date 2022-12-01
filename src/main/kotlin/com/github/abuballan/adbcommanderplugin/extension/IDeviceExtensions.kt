package com.github.abuballan.adbcommanderplugin.extension

import com.android.ddmlib.IDevice
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiver
import com.github.abuballan.adbcommanderplugin.common.receiver.ShellReceiverResult
import java.util.concurrent.TimeUnit

private const val MAX_TIME_TO_OUTPUT_RESPONSE = 15L

fun IDevice.getDeviceName(): String {
    return name
        .replace(serialNumber, "")
        .replace("[-_]".toRegex(), " ")
        .replace("[\\[\\]]".toRegex(), "")
}

fun <RESULT> IDevice.execute(command: String, receiver: ShellReceiver<RESULT>): ShellReceiverResult<RESULT> {
    return runCatching {
        executeShellCommand(command, receiver, MAX_TIME_TO_OUTPUT_RESPONSE, TimeUnit.SECONDS)
    }.fold(
        onSuccess = {
            receiver.getResult(receiver.getOutput())
        },
        onFailure = { error ->
            ShellReceiverResult.Error(error)
        }
    )
}


fun IDevice.getApiVersion() = version.apiLevel