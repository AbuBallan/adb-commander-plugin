package com.github.abuballan.adbcommanderplugin.ui.deviceChooser

import com.intellij.openapi.util.Condition
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.scale.JBUIScale
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.JPanel

class DeviceChooserPanel: JPanel(BorderLayout()) {
    private val deviceChooserComboBox = object : DeviceChooserComboBox() {
        override fun processKeyEvent(e: KeyEvent?) {
            super.processKeyEvent(e)
        }

        override fun processMouseEvent(e: MouseEvent?) {
            super.processMouseEvent(e)
        }


    }.apply {
//        addFocusListener(object : FocusListener {
//            override fun focusGained(e: FocusEvent?) {
//                super
//            }
//
//            override fun focusLost(e: FocusEvent?) {
//
//            }
//
//        })
        isFocusable = true
    }


    init {
        add(deviceChooserComboBox, BorderLayout.CENTER)
    }

    override fun requestFocusInWindow(): Boolean {
        return deviceChooserComboBox.requestFocusInWindow()
    }

    override fun requestFocus() {
        IdeFocusManager.getGlobalInstance()
            .doWhenFocusSettlesDown { IdeFocusManager.getGlobalInstance().requestFocus(deviceChooserComboBox, true) }
    }


}