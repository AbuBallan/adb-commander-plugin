package com.github.abuballan.adbcommanderplugin.services

import com.github.abuballan.adbcommanderplugin.model.Constants.FILE_VARIABLE_COLLECTION_PERSISTENT
import com.github.abuballan.adbcommanderplugin.model.VariableCollectionList
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service
@State(
    name = "variable_collection_persistent",
    storages = [Storage(FILE_VARIABLE_COLLECTION_PERSISTENT)]
)
class VariableCollectionPersistentService : PersistentStateComponent<VariableCollectionList> {

    var collection: VariableCollectionList = VariableCollectionList(ArrayList())

    override fun getState(): VariableCollectionList {
        return collection
    }

    override fun loadState(variables: VariableCollectionList) {
        this.collection = variables
    }

    companion object {
        fun getInstance(project: Project): VariableCollectionPersistentService = project.service()
    }
}
