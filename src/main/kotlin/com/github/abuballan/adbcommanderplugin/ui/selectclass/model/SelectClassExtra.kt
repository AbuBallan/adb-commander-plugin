package com.github.abuballan.adbcommanderplugin.ui.selectclass.model

sealed class SelectClassExtra {

    data class DeviceName(val deviceName: String) : SelectClassExtra()

    data class ClassName(val className: String) : SelectClassExtra()
}