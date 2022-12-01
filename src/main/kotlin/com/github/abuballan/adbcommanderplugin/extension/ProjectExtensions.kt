package com.github.abuballan.adbcommanderplugin.extension

import com.android.tools.idea.gradle.project.model.AndroidModuleModel
import com.android.tools.idea.run.AndroidRunConfiguration
import com.android.tools.idea.run.ConnectedAndroidDevice
import com.android.tools.idea.run.LaunchableAndroidDevice
import com.android.tools.idea.run.editor.DeployTarget
import com.android.tools.idea.run.editor.DeployTargetContext
import com.android.tools.idea.util.androidFacet
import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.intellij.execution.RunManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import org.jetbrains.android.facet.AndroidFacet

private val deployTargetContext = DeployTargetContext()

fun Project.getSelectedAdbCommandParams(): AdbCommandPrams {

    val selectedConfiguration = RunManager.getInstance(this).selectedConfiguration
        ?: throw RuntimeException("Please check the project configurations")

    val androidRunConfiguration = selectedConfiguration.configuration as? AndroidRunConfiguration
        ?: throw RuntimeException("Please check the project configurations, is it android app configurations?")

    val androidFacet = androidRunConfiguration.configurationModule?.module?.androidFacet
        ?: throw RuntimeException("Please check the project configurations, is the app module selected?")

    val packageId =
        AndroidModuleModel.get(androidFacet)?.applicationId ?: throw RuntimeException("Sorry, application id error")

    val deployDevices = getDeployTarget(this, androidFacet)?.getDevices(androidFacet)?.devices

    if (deployDevices == null || deployDevices.size < 1) {
        throw RuntimeException("Please select a device")
    }

    val selectedDevices = deployDevices.map { device ->
        when (device) {
            is ConnectedAndroidDevice -> device.launchedDevice.get()
            is LaunchableAndroidDevice -> device.launchedDevice.get()
            else -> throw RuntimeException("Unknown Error...")
        }
    }

    if (selectedDevices.isEmpty()) throw RuntimeException("Unknown Error...")

    return AdbCommandPrams(
        project = this,
        devices = selectedDevices,
        facet = androidFacet,
        packageId = packageId
    )
}

private fun getDeployTarget(project: Project, facet: AndroidFacet): DeployTarget? {
    val currentDeployTargetProvider = deployTargetContext.currentDeployTargetProvider
    return if (currentDeployTargetProvider.requiresRuntimePrompt(project))
        currentDeployTargetProvider.showPrompt(facet)
    else
        currentDeployTargetProvider.getDeployTarget(project)
}

fun Project.openClassByName(className: String) {
    val psiClass = getPsiClassByName(this, className)
    psiClass?.let { psiClass ->
        openPsiClass(psiClass)
    } ?: showNotification(
        NotificationType.ERROR,
        "ADB Commander",
        "<b>$className</b> not found in your code"
    )
}

fun Project.openClassByShortName(className: String) {
    val psiClass = getPsiClassByShortName(className)
    if (psiClass.isEmpty()) {
        showNotification(
            NotificationType.ERROR,
            "ADB Commander",
            "<b>$className</b> not found in your code"
        )
    } else {
        openPsiClass(psiClass[0])
    }
}

private fun getPsiClassByName(project: Project, className: String) = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project))

private fun Project.openPsiClass(psiClass: PsiClass) {
    OpenFileDescriptor(this, psiClass.containingFile.virtualFile, 1, 0).navigateInEditor(this, false)
}

fun Project.getPsiClassByShortName(className: String) = PsiShortNamesCache.getInstance(this).getClassesByName(
    className, GlobalSearchScope.allScope(this)
)