package com.space4414.kiyo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context,
) {
    companion object {
        const val PLAYBACK_CHANNEL_ID = "kiyo_playback"
        const val PLAYBACK_CHANNEL_NAME = "Kiyo Playback"
    }

    /**
     * Creates the notification channel required on API 26+.
     * Safe to call on all API levels — guarded by SDK_INT check.
     */
    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService<NotificationManager>() ?: return
            val channel = NotificationChannel(
                PLAYBACK_CHANNEL_ID,
                PLAYBACK_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Kiyo music playback controls"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
