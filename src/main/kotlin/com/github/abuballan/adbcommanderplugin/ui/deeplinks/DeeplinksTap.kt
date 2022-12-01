package com.github.abuballan.adbcommanderplugin.ui.deeplinks

import com.github.abuballan.adbcommanderplugin.model.DeepLinkInfo
import com.github.abuballan.adbcommanderplugin.ui.list.ListTransferHandler
import com.github.abuballan.adbcommanderplugin.ui.utils.createActionButton
import com.github.abuballan.adbcommanderplugin.ui.utils.createPanelWithTopControls
import com.github.abuballan.adbcommanderplugin.ui.utils.pushToNorthwest
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.datatransfer.DataFlavor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class DeeplinksTap(
    private val project: Project,
    private val modifiableItems: ArrayList<DeepLinkInfo>,
    private val getVariables: () -> ArrayList<Array<String>>,
) {

    private lateinit var listAndEditorSplitter: JBSplitter
    private lateinit var jList: JBList<DeepLinkInfo>
    private val listModel = DefaultListModel<DeepLinkInfo>()

    // Stores UI in memory to emulate tab-like feature where the UI
    // components are maintained even after navigating to different tabs
    private val editUiTracker = HashMap<String, EditorTap>()

    private val listMouseAdapter = object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            if (SwingUtilities.isRightMouseButton(e)) {
                jList.selectedIndex = jList.locationToIndex(e.point)
                setUpPopUpMenu(jList, e)
            }
        }

        override fun mouseClicked(e: MouseEvent?) {
            if (e?.clickCount == 2) {
//                editUiTracker[jList.selectedValue.id]?.onRunClicked()
            }
        }
    }

    private fun setUpPopUpMenu(list: JBList<DeepLinkInfo>, e: MouseEvent) {
        val menu = JPopupMenu()
        val run = JMenuItem("Run", AllIcons.Actions.Execute).apply {
            addActionListener { }
        }
        val add = JMenuItem("Add", AllIcons.General.Add).apply {
            addActionListener { onAddNewListItemClicked() }
        }
        val clone = JMenuItem("Clone", AllIcons.Actions.Copy).apply {
            addActionListener { onCloneListItemClicked() }
        }
        val delete = JMenuItem("Delete", AllIcons.General.Remove).apply {
            addActionListener { onDeleteListItemClicked() }
        }

        menu.add(run)
        menu.add(add)
        menu.add(clone)
        menu.add(delete)
        menu.show(list, e.point.x, e.point.y)
    }

    private val listSelectionListener = object : ListSelectionListener {
        override fun valueChanged(e: ListSelectionEvent?) {
            val selectedValue = jList.selectedValue

            // selectedValue will be null when a list item is deleted and before next item is selected
            if (selectedValue == null) {
                if (listModel.isEmpty) {
                    listAndEditorSplitter.secondComponent = null
                }
                return
            }

            if (editUiTracker.containsKey(selectedValue.id)) {
                listAndEditorSplitter.secondComponent = editUiTracker[selectedValue.id]?.panel
                return
            }

            val editorTab = EditorTap(
                project = project,
                deepLinkInfoInMemory = jList.selectedValue,
                getVariables = getVariables,
                onDisplayNameUpdated = { detail -> onEditorUpdated(detail) }
            )
            editUiTracker[jList.selectedValue?.id!!] = editorTab
            listAndEditorSplitter.secondComponent = editorTab.panel
        }
    }

    private fun onEditorUpdated(deepLinkInfo: DeepLinkInfo) {
        val index = listModel.toArray().indexOfFirst { (it as DeepLinkInfo).id == deepLinkInfo.id }
        listModel.set(index, deepLinkInfo)
    }

    internal fun get(): JComponent {
        listAndEditorSplitter = JBSplitter(false, 0.3f, 0.2f, 0.7f).apply {
            firstComponent = createLeftPanel()
            secondComponent = null
        }

        // Initially select first item in the list, if available
        if (!listModel.isEmpty) {
            jList.selectedIndex = 0
        }

        return listAndEditorSplitter
    }

    private fun createLeftPanel(): JPanel {
        val mainPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()

        gbc.anchor = GridBagConstraints.NORTHWEST
        gbc.fill = GridBagConstraints.BOTH
        gbc.gridx = 0
        gbc.gridy = 0
        mainPanel.add(setUpList(), gbc)

        gbc.pushToNorthwest(mainPanel)
        return mainPanel
    }

    private fun setUpList(): JPanel {
        JBList<DeepLinkInfo>().apply {
            jList = this
            accessibleContext.accessibleName = "DeepLinks list"
            model = listModel
            listModel.addAll(modifiableItems)

            cellRenderer = ListCellRenderer { _, value, _, _, _ ->
                JBLabel("<html>${value?.name}</html>").apply {
                    icon = AllIcons.Actions.Execute
                }
            }
            fixedCellHeight = 25
            setEmptyText("""Click "+" to create new deeplink""")

            // Setup DnD
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            dropMode = DropMode.INSERT
            dragEnabled = true
            transferHandler = ListTransferHandler<DeepLinkInfo>(LIST_DATA_FLAVOR) { from, to ->
                val fromItem = modifiableItems[from]
                modifiableItems.removeAt(from)
                modifiableItems.add(to, fromItem)
                selectedIndex = to
            }

            addListSelectionListener(listSelectionListener)
            addMouseListener(listMouseAdapter)
        }

        return createPanelWithTopControls(
            createActionButton("Add", AllIcons.General.Add) { onAddNewListItemClicked() }.apply {
                accessibleContext.accessibleName = "Add new deeplink"
            },
            createActionButton("Remove", AllIcons.General.Remove) { onDeleteListItemClicked() },
            createActionButton("Clone", AllIcons.Actions.Copy) { onCloneListItemClicked() },
            minWidth = 180,
            bottomComponent = JBScrollPane(jList)
        )
    }

    private fun onAddNewListItemClicked() {
        val dummy = DeepLinkInfo(
            id = UUID.randomUUID().toString(),
            name = "(New DeepLink)",
            deepLink = ""
        )

        // Add new item to top of list and select it
        modifiableItems.add(0, dummy)
        listModel.add(0, dummy)
        jList.selectedIndex = 0
    }

    private fun onDeleteListItemClicked() {
        val selectedIndex = jList.selectedIndex
        if (selectedIndex == -1) return

        val result: Int = Messages.showYesNoDialog(
            "Are you sure you want to delete ${listModel.get(selectedIndex).name}?",
            "Confirm Delete",
            AllIcons.General.Warning
        )

        if (result == Messages.NO) return

        editUiTracker.remove(listModel.get(selectedIndex).id)
        modifiableItems.removeAt(selectedIndex)
        listModel.remove(selectedIndex)
        // Select next item from the list
        if (listModel.isEmpty) return
        jList.selectedIndex = when (selectedIndex == jList.model.size) {
            true -> jList.model.size - 1
            false -> selectedIndex
        }
    }

    private fun onCloneListItemClicked() {
        val selectedIndex = jList.selectedIndex
        if (selectedIndex == -1) return

        val selectedItem = jList.selectedValue

        val clone = selectedItem.copy(
            id = UUID.randomUUID().toString(),
            name = "Copy of ${selectedItem.name}"
        )

        modifiableItems.add(0, clone)
        listModel.add(0, clone)

        jList.selectedIndex = 0
    }

    companion object {
        private val LIST_DATA_FLAVOR = DataFlavor(DeepLinkInfo::class.java, "java/${DeepLinkInfo::class.simpleName}")
    }

}