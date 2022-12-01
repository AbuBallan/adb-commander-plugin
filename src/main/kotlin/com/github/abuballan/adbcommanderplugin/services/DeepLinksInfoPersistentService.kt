package com.github.abuballan.adbcommanderplugin.services

import com.github.abuballan.adbcommanderplugin.model.Constants
import com.github.abuballan.adbcommanderplugin.model.Constants.FILE_DEEP_LINKS_INFO_PERSISTENT
import com.github.abuballan.adbcommanderplugin.model.DeepLinkInfo
import com.github.abuballan.adbcommanderplugin.model.DeepLinksInfoList
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import java.util.*

@Service
@State(
    name = "deep_links_persistent",
    storages = [Storage(FILE_DEEP_LINKS_INFO_PERSISTENT)]
)
class DeepLinksInfoPersistentService : PersistentStateComponent<DeepLinksInfoList> {

    var deepLinksInfoListState: DeepLinksInfoList = DeepLinksInfoList(DEFAULT_DEEP_LINKS)

    override fun getState(): DeepLinksInfoList {
        return deepLinksInfoListState
    }

    override fun loadState(deepLinksInfoListState: DeepLinksInfoList) {
        this.deepLinksInfoListState = deepLinksInfoListState
    }

    companion object {
        fun getInstance(project: Project): DeepLinksInfoPersistentService = project.service()
        private val DEFAULT_DEEP_LINKS = arrayListOf(
            DeepLinkInfo(
                id = UUID.randomUUID().toString(),
                name = "Development Settings",
                action = "android.settings.APPLICATION_DEVELOPMENT_SETTINGS",
            ),
            DeepLinkInfo(
                id = UUID.randomUUID().toString(),
                name = "Device Information",
                action = "android.settings.DEVICE_INFO_SETTINGS",
            ),
            DeepLinkInfo(
                id = UUID.randomUUID().toString(),
                name = "Device Languages",
                action = "android.settings.LOCALE_SETTINGS",
            ),
            DeepLinkInfo(
                id = UUID.randomUUID().toString(),
                name = "Device Settings",
                action = "android.settings.SETTINGS",
            ),
            DeepLinkInfo(
                id = UUID.randomUUID().toString(),
                name = "WIFI Settings",
                action = "android.settings.WIFI_SETTINGS",
            ),
        )
    }
}
