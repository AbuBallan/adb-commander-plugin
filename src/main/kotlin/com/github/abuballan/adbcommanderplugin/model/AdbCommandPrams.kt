package com.github.abuballan.adbcommanderplugin.model

import com.android.ddmlib.IDevice
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet

data class AdbCommandPrams(
    val project: Project,
    val devices: List<IDevice>,
    val facet: AndroidFacet,
    val packageId: String
)