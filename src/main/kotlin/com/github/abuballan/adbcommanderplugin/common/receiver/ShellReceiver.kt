package com.github.abuballan.adbcommanderplugin.common.receiver

import com.android.ddmlib.IShellOutputReceiver

abstract class ShellReceiver<RESULT> : IShellOutputReceiver {
    private val output = StringBuilder()

    override fun addOutput(data: ByteArray, offset: Int, length: Int) {
        output.append(String(data, offset, length))
    }

    override fun flush() = Unit

    override fun isCancelled() = false

    fun getOutput() = output.toString().trim()

    abstract fun getResult(output: String): ShellReceiverResult<RESULT>

}