package com.github.abuballan.adbcommanderplugin.ui.quickcommand

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SimpleColoredComponent
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

class QuickAdbCommandDialog(
    project: Project
): DialogWrapper(project, true) {


    override fun createNorthPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbConstraints = GridBagConstraints()
        gbConstraints.insets = JBUI.insetsBottom(UIUtil.DEFAULT_VGAP)
        gbConstraints.fill = GridBagConstraints.NONE
        gbConstraints.weightx = 1.0
        gbConstraints.weighty = 1.0
        gbConstraints.anchor = GridBagConstraints.WEST
        val coloredComponent = SimpleColoredComponent()
        coloredComponent.ipad = JBUI.emptyInsets()
        coloredComponent.setMyBorder(null)
        //configureLabelComponent(coloredComponent)
        panel.add(coloredComponent, gbConstraints)
        return panel
    }

    override fun createCenterPanel(): JComponent {
        val gridBagLayout = GridBagLayout()
        val jPanel = JPanel(gridBagLayout)

        return jPanel
    }

    override fun getPreferredFocusedComponent(): JComponent? = null
}