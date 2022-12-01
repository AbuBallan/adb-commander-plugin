package com.github.abuballan.adbcommanderplugin.command

sealed class AdbCommandResult<RESULT> {

    data class Success<RESULT>(
        val deviceName: String,
        val result: RESULT
    ) : AdbCommandResult<RESULT>()

    data class Error<RESULT>(
        val deviceName: String,
        val errorMsg: String
    ) : AdbCommandResult<RESULT>()

}