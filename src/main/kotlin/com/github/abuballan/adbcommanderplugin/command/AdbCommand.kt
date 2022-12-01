package com.github.abuballan.adbcommanderplugin.command

import com.android.ddmlib.IDevice
import com.github.abuballan.adbcommanderplugin.model.AdbCommandPrams
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.android.facet.AndroidFacet

abstract class AdbCommand<RESULT>(
    private val adbCommandPrams: AdbCommandPrams
) {

    open fun dependsOn(): AdbCommand<*>? = null

    fun execute(onFinish: (() -> Unit) = {}) {
        CoroutineScope(Dispatchers.Main).launch {
            val results = withContext(Dispatchers.IO) { getResults() }
            handleResults(adbCommandPrams.project, adbCommandPrams.packageId, results)
            onFinish.invoke()
        }
    }

    fun getResults(): List<AdbCommandResult<RESULT>> {
        return if (dependsOn() == null) {
            adbCommandPrams.devices.map { device ->
                runCommand(adbCommandPrams.project, device, adbCommandPrams.facet, adbCommandPrams.packageId, null)
            }
        } else {
            val dependencyResults = dependsOn()?.getResults()
            dependencyResults?.mapIndexed { index, dependencyResult ->
                runCommand(
                    adbCommandPrams.project,
                    adbCommandPrams.devices[index],
                    adbCommandPrams.facet,
                    adbCommandPrams.packageId,
                    dependencyResult
                )
            } ?: listOf()
        }
    }


    abstract fun runCommand(
        project: Project,
        device: IDevice,
        facet: AndroidFacet,
        packageName: String,
        dependencyResult: AdbCommandResult<*>?
    ): AdbCommandResult<RESULT>

    abstract fun handleResults(project: Project, packageId: String, results: List<AdbCommandResult<RESULT>>)

}