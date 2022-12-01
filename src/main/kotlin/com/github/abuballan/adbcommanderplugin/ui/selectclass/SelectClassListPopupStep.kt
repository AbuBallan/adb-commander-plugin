package com.github.abuballan.adbcommanderplugin.ui.selectclass

import com.github.abuballan.adbcommanderplugin.extension.openClassByName
import com.github.abuballan.adbcommanderplugin.ui.selectclass.model.SelectClassExtra
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.ListPopupStep
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep

class SelectClassListPopupStep(private val project: Project, private val title: String, private val list: List<SelectClassExtra>, private val onSelectClass: (String) -> Unit) :
    ListPopupStep<SelectClassExtra> {
    override fun getTitle() = title

    override fun getValues(): MutableList<SelectClassExtra> {
        return list.toMutableList()
    }

    override fun getTextFor(item: SelectClassExtra?) = when (item) {
        is SelectClassExtra.DeviceName -> item.deviceName
        is SelectClassExtra.ClassName -> item.className
        else -> ""
    }

    override fun isSelectable(item: SelectClassExtra?) = when (item) {
        is SelectClassExtra.DeviceName -> false
        is SelectClassExtra.ClassName -> true
        else -> false
    }

    override fun getSeparatorAbove(item: SelectClassExtra?) =
        if (item is SelectClassExtra.DeviceName) ListSeparator() else null

    override fun onChosen(item: SelectClassExtra?, p1: Boolean): PopupStep<*>? {
        if (item is SelectClassExtra.ClassName)
            onSelectClass.invoke(item.className)
        return null
    }

    override fun canceled() = Unit

    override fun isMnemonicsNavigationEnabled() = false

    override fun getMnemonicNavigationFilter() = null

    override fun isSpeedSearchEnabled() = false

    override fun getSpeedSearchFilter() = null

    override fun isAutoSelectionEnabled() = false

    override fun getFinalRunnable() = null

    override fun getDefaultOptionIndex() = 0

    override fun getIconFor(p0: SelectClassExtra?) = null

    override fun hasSubstep(p0: SelectClassExtra?) = false

}