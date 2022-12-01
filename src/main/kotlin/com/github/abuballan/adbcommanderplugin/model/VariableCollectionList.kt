package com.github.abuballan.adbcommanderplugin.model

/**
 * Represents a collection of set of variables such as Staging and Production
 */
data class VariableCollectionList(
    var items: ArrayList<VariableCollection> = ArrayList()
)
