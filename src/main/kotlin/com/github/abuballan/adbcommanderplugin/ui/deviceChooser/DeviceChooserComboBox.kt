package com.github.abuballan.adbcommanderplugin.ui.deviceChooser

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.TitledSeparator
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.UIUtil
import java.awt.Component
import javax.swing.DefaultComboBoxModel
import javax.swing.JList


open class DeviceChooserComboBox : ComboBox<DeviceDescriptor>() {

    private val model = DefaultComboBoxModel<DeviceDescriptor>()

    init {
        setModel(model)
        setRenderer(MyRenderer())
        setMinimumAndPreferredWidth(getPreferredWidth())
        //isSwingPopup = false
        //isRequestFocusEnabled = false
    }

    fun setData(data: List<DeviceDescriptor>) {
        model.addAll(data)
    }

    private fun getPreferredWidth(): Int {
        val component = getEditor().editorComponent
        val fontMetrics = component.getFontMetrics(component.font)
        return fontMetrics.stringWidth("Samsung Galaxy Nexus Android 4.1 (API 17)")
    }

    override fun setSelectedItem(item: Any?) {
        if (item !is DeviceSeparator)
            super.setSelectedItem(item)
    }

    override fun setSelectedIndex(index: Int) {
        val item = getItemAt(index)
        if (item !is DeviceSeparator)
            super.setSelectedIndex(index)
    }

    private class MyRenderer : SimpleListCellRenderer<DeviceDescriptor>() {

        val separator = TitledSeparator()

        override fun customize(
            list: JList<out DeviceDescriptor>,
            value: DeviceDescriptor?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ) {
            value?.let {
                icon = it.icon
                text = it.displayName
            }
        }

        override fun getListCellRendererComponent(
            list: JList<out DeviceDescriptor>?,
            value: DeviceDescriptor?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            if (value is DeviceSeparator) {
                separator.text = value.displayName
                separator.border =
                    if (index == -1) null else JBEmptyBorder(UIUtil.DEFAULT_VGAP, 2, UIUtil.DEFAULT_VGAP, 0)
                return separator
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        }
    }

}

class DeviceSeparator(text: String) : DeviceDescriptor(text)
