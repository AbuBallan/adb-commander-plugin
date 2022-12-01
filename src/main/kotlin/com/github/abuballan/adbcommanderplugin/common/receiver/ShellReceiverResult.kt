package com.github.abuballan.adbcommanderplugin.common.receiver

sealed class ShellReceiverResult<RESULT> {

    data class Success<RESULT>(val result: RESULT): ShellReceiverResult<RESULT>()

    data class Error<RESULT>(val error: Throwable): ShellReceiverResult<RESULT>()

}