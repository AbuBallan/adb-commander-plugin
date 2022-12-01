package com.github.abuballan.adbcommanderplugin.model

data class DeepLinkInfo(
    var id: String? = null,
    var name: String? = null,
    var deepLink: String? = null,
    var action: String? = null,
    var category: String? = null
)