package com.github.abuballan.adbcommanderplugin.action

import com.android.tools.idea.gradle.project.model.AndroidModuleModel
import com.android.tools.idea.run.AndroidRunConfiguration
import com.android.tools.idea.run.editor.DeployTarget
import com.android.tools.idea.run.editor.DeployTargetContext
import com.android.tools.idea.util.androidFacet
import com.github.abuballan.adbcommanderplugin.extension.showNotification
import com.github.abuballan.adbcommanderplugin.ui.deviceChooser.DeviceChooserComboBox
import com.github.abuballan.adbcommanderplugin.ui.deviceChooser.DeviceDescriptor
import com.intellij.execution.RunManager
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.util.Disposer
import com.intellij.ui.PopupBorder
import com.intellij.ui.popup.list.ListPopupImpl
import org.jetbrains.android.facet.AndroidFacet
import java.awt.BorderLayout
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable

class StartQuickAdbCommandDialogAction : AdbAction() {

    private val deployTargetContext = DeployTargetContext()

    override fun actionPerformed(event: AnActionEvent, project: Project) {

        val selectedConfiguration = RunManager.getInstance(project).selectedConfiguration

        if (selectedConfiguration == null) {
            project.showNotification(
                NotificationType.INFORMATION,
                "Hello Device",
                "no selceted device"
            )
            return
        }

        val androidRunConfiguration = selectedConfiguration.configuration as? AndroidRunConfiguration

        if (androidRunConfiguration == null) {
            project.showNotification(
                NotificationType.INFORMATION,
                "Hello Device",
                "selected configuration error"
            )
            return
        }

        val module = androidRunConfiguration.configurationModule?.module
        val androidFacet = module?.androidFacet

        if (androidFacet == null) {
            project.showNotification(
                NotificationType.INFORMATION,
                "Hello Device",
                "android facet error"
            )
            return
        }

        val applicationId = AndroidModuleModel.get(androidFacet)?.applicationId


        if (applicationId == null) {
            project.showNotification(
                NotificationType.INFORMATION,
                "Hello Device",
                "error application id"
            )
            return
        }

        val deployTarget = getDeployTarget(project, androidFacet)

        val devices = deployTarget?.getDevices(androidFacet)?.devices

        if (devices == null || devices.size < 1) {
            project.showNotification(
                NotificationType.INFORMATION,
                "Hello Device",
                "no selected device"
            )
            return
        }


        val devicesText = devices.map {
            it.serial
        }.toString()


        project.showNotification(
            NotificationType.INFORMATION,
            "Hello Device",
            "$devicesText - $applicationId"
        )


        //showUsagesPopUp(project)
        //ShowIntentionActionsAction().actionPerformed(event)

        // ShowUsagesAction().actionPerformed(event)

        // create list popup step
//        val listPopupStep = ListPopupStepImpl()
//
//
//        // create popup list
//        val popupList = CustomListPopup(project, listPopupStep)
//
//        popupList.showCenteredInCurrentWindow(project)
//
//        popupList.doSomething()

//        val actionGroup = DefaultActionGroup()
//        actionGroup.add(object : AnAction("Hello"), SpeedsearchAction {
//            override fun actionPerformed(e: AnActionEvent) {
//
//            }
//        })
//        actionGroup.add(object : AnAction("Hello1"), SpeedsearchAction {
//            override fun actionPerformed(e: AnActionEvent) {
//
//            }
//        })
//        actionGroup.add(object : AnAction("Hello2"), SpeedsearchAction {
//            override fun actionPerformed(e: AnActionEvent) {
//
//            }
//        })
//        actionGroup.add(object : AnAction("Hello3"), SpeedsearchAction {
//            override fun actionPerformed(e: AnActionEvent) {
//
//            }
//        })
//        actionGroup.add (object : AnAction("Hello4"), SpeedsearchAction {
//            override fun actionPerformed(e: AnActionEvent) {
//
//            }
//        })


        //SwitchFileBasedIndexStorageAction().actionPerformed(event)

        //StartQuickAdbCommandPopup(ListPopupStepImpl()).showCenteredInCurrentWindow(event.project!!)


//
//        JBPopupFactory.getInstance().createConfirmation(
//            "tEXT",
//            Runnable {
//
//            },
//            -1
//        ).showCenteredInCurrentWindow(event.project!!)

//        ATestPopup(
//            "Hello",
//            actionGroup,
//            event.dataContext,
//            null,
//            false
//        ).showCenteredInCurrentWindow(project)
//        val content = popupList.content
//       // val deviceChooserComboBox = DeviceChooserComboBox()
        //      val componentPopupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(
//            JPanel().apply {
//                add(popupList.content)
//            },
//            popupList.content.rootPane.componentPopupMenu
//        )
//
//        componentPopupBuilder.createPopup().showCenteredInCurrentWindow(project)

        //JBPopupFactory.getInstance().createActionGroupPopup()

        //showUsagesPopUp(event.project!!)

    }

    private fun getDeployTarget(project: Project, facet: AndroidFacet): DeployTarget? {
        val currentDeployTargetProvider = deployTargetContext.currentDeployTargetProvider
        return if (currentDeployTargetProvider.requiresRuntimePrompt(project))
            currentDeployTargetProvider.showPrompt(facet)
        else
            currentDeployTargetProvider.getDeployTarget(project)
    }

    private fun showUsagesPopUp(project: Project) {

        // ???
        ApplicationManager.getApplication().assertIsDispatchThread()

        // title
        val title = "Hello Ballan"

        // create JTable
        val jtable = JTable(
            4, 4
        )

//        // for testing
        val elementList = listOf(
            "Ballan 1",
            "Ballan 2",
        )

        // create PopupChooserBuilder
        val popupChooserBuilder = JBPopupFactory.getInstance().createPopupChooserBuilder(
            jtable
        ).apply {
            // set title
            setTitle("<body><nobr>$title</nobr></body>")

            // set ad text
            setAdText("Hello AdText")

            // make popup movable
            setMovable(true)

            // make popup resizable
            setResizable(true)

            // ???
            setCancelKeyEnabled(true)

            // ???
            setDimensionServiceKey("StartQuickAdbCommandDialogAction.dimensionServiceKey")
        }

        // command button
        //popupChooserBuilder.setCommandButton()

        // north component
        // for test

        val contentDisposable = Disposer.newDisposable()
//        val scopeChooserCombo =
//            ScopeChooserCombo(project, false, false, "Ballan")
//        val scopeComboBox = scopeChooserCombo.comboBox
//        scopeComboBox.setMinimumAndPreferredWidth(JBUIScale.scale(200))
//        scopeComboBox.addItemListener { event: ItemEvent ->
////            if (event.stateChange == ItemEvent.SELECTED) {
////                val scope = scopeChooserCombo.selectedScope
////                if (scope != null) {
////                    UsageViewStatisticsCollector.logScopeChanged(
////                        project, actionHandler.getSelectedScope(), scope,
////                        actionHandler.getTargetClass()
////                    )
////                    ShowUsagesAction.cancel(popupRef.get())
////                    ShowUsagesAction.showElementUsages(parameters, actionHandler.withScope(scope))
////                }
////            }
//        }
//
//        Disposer.register(contentDisposable, scopeChooserCombo)
//        scopeComboBox.putClientProperty("JComboBox.isBorderless", Boolean.TRUE)
//        scopeChooserCombo.setButtonVisible(false)

        val deviceChooserCombo = DeviceChooserComboBox()

//        deviceChooserCombo.comboBox.apply {
//            putClientProperty("JComboBox.isBorderless", Boolean.TRUE)
//        }

        //Disposer.register(contentDisposable, deviceChooserCombo)

        // deviceChooserCombo.setButtonVisible(false)

        val northComponent = JPanel().apply {
            add(deviceChooserCombo)
        }

        popupChooserBuilder.setNorthComponent(
            northComponent
        )


        popupChooserBuilder.createPopup().showCenteredInCurrentWindow(project)


        //JBPopupFactory.getInstance().createActionGroupPopup()

    }

}


class ListPopupStepImpl : ListPopupStep<String> {
    override fun getTitle() = "Hello Title"

    override fun getValues(): MutableList<String> {
        return mutableListOf(
            "Step 1",
            "Step 2"
        )
    }

    override fun isSelectable(value: String?) = value != null

    override fun getIconFor(value: String?): Icon {
        return AllIcons.Icons.Ide.NextStep
    }

    override fun getTextFor(value: String?) = value ?: ""

    override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
        return null
    }

    override fun hasSubstep(selectedValue: String?): Boolean = selectedValue.isNullOrEmpty().not()

    override fun canceled() {

    }

    override fun isMnemonicsNavigationEnabled() = false

    override fun getMnemonicNavigationFilter(): MnemonicNavigationFilter<String>? {
        return null
    }

    override fun isSpeedSearchEnabled(): Boolean {
        return false
    }

    override fun getSpeedSearchFilter(): SpeedSearchFilter<String>? {
        return null
    }

    override fun isAutoSelectionEnabled(): Boolean {
        return true
    }

    override fun getFinalRunnable(): Runnable? {
        return null
    }

    override fun getSeparatorAbove(value: String?): ListSeparator? {
        return ListSeparator()
    }

    override fun getDefaultOptionIndex(): Int {
        return 0
    }
}

class CustomListPopup(project: Project?, aStep: ListPopupStep<*>) : ListPopupImpl(project, aStep) {

    lateinit var contentPanel: MyContentPanel


    override fun createContentPanel(
        resizable: Boolean,
        border: PopupBorder,
        isToDrawMacCorner: Boolean
    ): MyContentPanel {
        contentPanel = super.createContentPanel(resizable, border, isToDrawMacCorner)
        return contentPanel
    }

    fun doSomething() {
        contentPanel.add(DeviceChooserComboBox(), BorderLayout.NORTH)
    }
}


class StartQuickAdbCommandPopup(aStep: ListPopupStep<*>) : ListPopupImpl(aStep) {

    private var list: JComponent? = null

    init {
        //setShowSubmenuOnHover(false)
    }

    override fun beforeShow(): Boolean {
        val beforeShow = super.beforeShow()


        // dispose the pop up from comobox it's not from here !!!!!
//        list?.mouseMotionListeners?.forEach {
//            list?.removeMouseMotionListener(it)
//        }
//        list?.mouseListeners?.forEach {
//            list?.removeMouseListener(it)
//        }

        val deviceChooserComboBox = DeviceChooserComboBox()
        deviceChooserComboBox.setData(
            listOf(
                DeviceDescriptor("Hello1"),
                DeviceDescriptor("Hello1"),
                DeviceDescriptor("Hello1"),
                DeviceDescriptor("Hello1"),
                DeviceDescriptor("Hello1")
            )
        )

        setHeaderComponent(deviceChooserComboBox)

        return beforeShow
    }

    override fun createContent(): JComponent {

        list = super.createContent()

        return list!!
    }


}