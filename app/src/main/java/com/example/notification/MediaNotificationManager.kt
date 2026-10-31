package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.ui.viewmodels.LocalTrack

object MediaNotificationManager {
    const val CHANNEL_ID = "dj_playback_channel"
    const val NOTIFICATION_ID = 1001

    const val ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE"
    const val ACTION_NEXT = "com.example.action.NEXT"
    const val ACTION_PREV = "com.example.action.PREV"
    const val ACTION_SYNC = "com.example.action.SYNC"
    const val ACTION_PLAY_A = "com.example.action.PLAY_A"
    const val ACTION_PLAY_B = "com.example.action.PLAY_B"
    const val ACTION_CUE_A = "com.example.action.CUE_A"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "DJ Musik-Wiedergabe"
            val descriptionText = "Steuerung der DJ-Decks und Musikwiedergabe im Hintergrund"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showOrUpdateNotification(
        context: Context,
        trackA: LocalTrack?,
        trackB: LocalTrack?,
        isPlayingA: Boolean,
        isPlayingB: Boolean,
        bpmA: Int,
        bpmB: Int,
        isSync: Boolean
    ) {
        createNotificationChannel(context)

        val isAnyPlaying = isPlayingA || isPlayingB
        val activeTrack = trackA ?: trackB
        val trackTitle = activeTrack?.title ?: "DJ Live Mix"
        val trackArtist = activeTrack?.artist ?: "Pioneer DJ Console"
        val deckStatus = when {
            isPlayingA && isPlayingB -> "DUAL PLAY (CH 1 & 2) • ${bpmA} BPM"
            isPlayingA -> "DECK A LIVE • ${bpmA} BPM"
            isPlayingB -> "DECK B LIVE • ${bpmB} BPM"
            else -> "PAUSIERT • ${bpmA} BPM"
        }

        // Open App Intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Previous Action
        val prevIntent = Intent(context, MediaNotificationReceiver::class.java).apply {
            action = ACTION_PREV
        }
        val prevPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play/Pause Action
        val playPauseIntent = Intent(context, MediaNotificationReceiver::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Next Action
        val nextIntent = Intent(context, MediaNotificationReceiver::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Sync Action
        val syncIntent = Intent(context, MediaNotificationReceiver::class.java).apply {
            action = ACTION_SYNC
        }
        val syncPendingIntent = PendingIntent.getBroadcast(
            context,
            4,
            syncIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isAnyPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val playPauseLabel = if (isAnyPlaying) "Pause" else "Play"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(trackTitle)
            .setContentText("$trackArtist • $deckStatus")
            .setSubText(if (isSync) "BEAT SYNC ON" else "Pioneer DJ")
            .setContentIntent(openAppPendingIntent)
            .setOngoing(isAnyPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_skip_previous, "Zurück", prevPendingIntent)
            .addAction(playPauseIcon, playPauseLabel, playPausePendingIntent)
            .addAction(R.drawable.ic_skip_next, "Weiter", nextPendingIntent)
            .addAction(R.drawable.ic_sync, if (isSync) "Sync Active" else "Sync", syncPendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$trackArtist\n$deckStatus\n${if (isSync) "Synchronisiert (Beatlock Active)" else "Master Deck A"}")
            )

        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.music_app_icon_1783244890356)
            if (largeIcon != null) {
                builder.setLargeIcon(largeIcon)
            }
        } catch (_: Exception) {}

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS might not yet be granted
        }
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
