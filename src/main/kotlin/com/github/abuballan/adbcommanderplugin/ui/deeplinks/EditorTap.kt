package com.github.abuballan.adbcommanderplugin.ui.deeplinks

import com.github.abuballan.adbcommanderplugin.command.AdbCommandResult
import com.github.abuballan.adbcommanderplugin.command.OpenDeepLinkCommand
import com.github.abuballan.adbcommanderplugin.extension.getSelectedAdbCommandParams
import com.github.abuballan.adbcommanderplugin.model.DeepLinkInfo
import com.github.abuballan.adbcommanderplugin.ui.utils.*
import com.github.abuballan.adbcommanderplugin.ui.utils.listener.SimpleDocumentListener
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.text.DefaultCaret

class EditorTap(
    private val project: Project,
    private val deepLinkInfoInMemory: DeepLinkInfo,
    private val getVariables: () -> ArrayList<Array<String>>,
    private val onDisplayNameUpdated: (DeepLinkInfo) -> Unit,
) {
    // UI components
    private lateinit var jHeader: JBTable
    private lateinit var jOutput: JBTextArea
    private lateinit var jOutputLabel: JBLabel


    internal var panel: JComponent = get()

    private fun get(): JComponent {
        val upperPanel = JPanel()
        upperPanel.layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.insets = Insets(4, 0, 4, 4)

        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.anchor = GridBagConstraints.CENTER
        gbc.gridx = 0
        gbc.gridy = 0
        upperPanel.add(JBLabel("Name"), gbc)

        gbc.gridx = 1
        gbc.gridy = 0
        gbc.gridwidth = 2
        upperPanel.add(JBTextField().apply {
            accessibleContext.accessibleName = "DeepLink name"
            text = deepLinkInfoInMemory.name
            document.addDocumentListener(object : SimpleDocumentListener() {
                override fun update(e: DocumentEvent?) {
                    deepLinkInfoInMemory.name = text
                    onDisplayNameUpdated(deepLinkInfoInMemory)
                }
            })
        }, gbc)

        gbc.gridwidth = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.anchor = GridBagConstraints.CENTER
        gbc.gridx = 0
        gbc.gridy = 2
        upperPanel.add(JBLabel("Deeplink"), gbc)

        gbc.gridx = 1
        gbc.gridy = 2
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.anchor = GridBagConstraints.NORTHWEST
        upperPanel.add(JBTextField().apply {
            accessibleContext.accessibleName = "Deeplink url"
            text = deepLinkInfoInMemory.deepLink
            document.addDocumentListener(object : SimpleDocumentListener() {
                override fun update(e: DocumentEvent?) {
                    deepLinkInfoInMemory.deepLink = text
                }
            })
        }, gbc)

        gbc.gridwidth = 1
        gbc.gridx = 2
        gbc.gridy = 2
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.NONE
        gbc.anchor = GridBagConstraints.EAST
        upperPanel.add(
            createActionButton("Run", AllIcons.Actions.Execute) {
                onRunClicked()
            }.apply {
                accessibleContext.accessibleName = "Execute deeplink"
            }, gbc
        )

        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.anchor = GridBagConstraints.CENTER
        gbc.gridx = 0
        gbc.gridy = 3
        upperPanel.add(JBLabel("Action"), gbc)

        gbc.gridx = 1
        gbc.gridy = 3
        gbc.gridwidth = 2
        upperPanel.add(JBTextField().apply {
            accessibleContext.accessibleName = "DeepLink action"
            text = deepLinkInfoInMemory.action
            document.addDocumentListener(object : SimpleDocumentListener() {
                override fun update(e: DocumentEvent?) {
                    deepLinkInfoInMemory.action = text
                }
            })
        }, gbc)

        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.anchor = GridBagConstraints.CENTER
        gbc.gridx = 0
        gbc.gridy = 4
        upperPanel.add(JBLabel("Category"), gbc)

        gbc.gridx = 1
        gbc.gridy = 4
        gbc.gridwidth = 2
        upperPanel.add(JBTextField().apply {
            accessibleContext.accessibleName = "DeepLink category"
            text = deepLinkInfoInMemory.category
            document.addDocumentListener(object : SimpleDocumentListener() {
                override fun update(e: DocumentEvent?) {
                    deepLinkInfoInMemory.category = text
                }
            })
        }, gbc)

        gbc.gridx = 0
        gbc.gridy = 5
        gbc.fill = GridBagConstraints.BOTH
        gbc.gridwidth = 3
        upperPanel.add(JPanel(), gbc)

        val lowerPanel = setUpOutputPanel()
        gbc.pushToNorthwest(upperPanel)

        val mainPanel = JPanel(GridBagLayout()).apply {
            add(JBSplitter(true, 0.40f).apply {
                firstComponent = upperPanel
                secondComponent = lowerPanel
            }, gbc)
            preferredSize = Dimension(500, 700)
        }
        return mainPanel
    }

    private fun onRunClicked() {
        jOutputLabel.text = "Loading..."
        runCatching {
            project.getSelectedAdbCommandParams()
        }
            .onSuccess { selectedAdbCommandParams ->
                jOutput.text = "Loading..."
                CoroutineScope(Dispatchers.Main).launch {
                    val results = withContext(Dispatchers.IO) {
                        OpenDeepLinkCommand(
                            substituteVariables(deepLinkInfoInMemory, getVariables()),
                            selectedAdbCommandParams
                        ).getResults()
                    }

                    if (results.all { it is AdbCommandResult.Error }) {
                        jOutputLabel.text = "<html><font color='#FFB6C1'>Fail</font></html>"
                    } else {
                        jOutputLabel.text = "<html><font color='#32CD32'>Success</font></html>"
                    }

                    jOutput.text = results.joinToString("\n") {
                        when (it) {
                            is AdbCommandResult.Success -> "${it.deviceName}\n${it.result}"
                            is AdbCommandResult.Error -> "${it.deviceName}\n${it.errorMsg}"
                        }
                    }

                }
            }
            .onFailure { error ->
                jOutputLabel.text = "<html><font color='red'>Exception</font></html>"
                jOutput.text = "${error.message}\n${error.stackTraceToString()}"
            }
    }

    private fun setUpOutputPanel(): JPanel {
        val outputPanel = JPanel()
        outputPanel.layout = BoxLayout(outputPanel, BoxLayout.Y_AXIS)
        outputPanel.add(JBLabel("Output").apply {
            jOutputLabel = this
            accessibleContext.accessibleName = "Deeplink output label"
            border = BorderFactory.createEmptyBorder(0, 0, 6, 0)
        })

        JBTextArea(1, 15).apply {
            jOutput = this
            accessibleContext.accessibleName = "Deeplink output"
            font = monoSpacedFont
            isEditable = false
            text = """Click "Play" icon or double-tap list item to see output"""
            caret = DefaultCaret().apply { updatePolicy = DefaultCaret.NEVER_UPDATE }
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        }

        val outputWithController = createPanelWithLeftControls(
            createToggleButton("Wrap", AllIcons.Actions.ToggleSoftWrap) {
                jOutput.wrapStyleWord = it
                jOutput.lineWrap = it
            },
            minWidth = 250,
            bottomComponent = JBScrollPane(jOutput)
        )

        outputPanel.add(outputWithController.apply {
            alignmentX = Component.LEFT_ALIGNMENT
        })

        return outputPanel
    }

}