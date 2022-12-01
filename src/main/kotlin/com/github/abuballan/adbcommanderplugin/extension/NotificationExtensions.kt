package com.github.abuballan.adbcommanderplugin.extension

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

private const val NOTIFICATION_GROUP = "adbCommanderNotification"

fun Project?.showNotification(type: NotificationType, title: String?, content: String) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup(NOTIFICATION_GROUP)
        .run {
            if (title.isNullOrEmpty())
                createNotification(
                    content,
                    type
                )
            else
                createNotification(
                    title,
                    content,
                    type
                )
        }
        .notify(this)
}