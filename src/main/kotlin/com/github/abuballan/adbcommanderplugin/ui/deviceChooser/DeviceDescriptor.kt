package com.github.abuballan.adbcommanderplugin.ui.deviceChooser

import com.intellij.openapi.util.ColoredItem
import java.awt.Color
import javax.swing.Icon

open class DeviceDescriptor(
    val displayName: String,
    val icon: Icon? = null,
    val itemColor: Color? = null
): ColoredItem {
    override fun getColor(): Color? = itemColor
}