package com.github.abuballan.adbcommanderplugin.ui.utils

import com.github.abuballan.adbcommanderplugin.model.DeepLinkInfo
import java.util.*

fun substituteVariables(
    deepLinkInfo: DeepLinkInfo,
    variableItems: List<Array<String>>
): DeepLinkInfo {
    val variables = variableItems.toMutableList()
    variables.add(arrayOf("UUID", UUID.randomUUID().toString()),)
    var deeplink = deepLinkInfo.deepLink
    var action = deepLinkInfo.action
    var category = deepLinkInfo.category

    for ((key, value) in variables) {
        val keyAsVariable = key.toVariableRepresentation()
        deeplink = deeplink?.replace(keyAsVariable, value)
        action = action?.replace(keyAsVariable, value)
        category = category?.replace(keyAsVariable, value)
    }

    return deepLinkInfo.copy(
        deepLink = deeplink,
        action = action,
        category = category
    )
}

fun String.toVariableRepresentation() = "$" + "{" + this + "}"
