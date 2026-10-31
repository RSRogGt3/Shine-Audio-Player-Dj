package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.notification.MediaNotificationManager
import com.example.notification.MediaNotificationReceiver
import com.example.ui.viewmodels.LocalMusicViewModel
import com.example.ui.viewmodels.LocalTrack

class DjQuickPlayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val vm = LocalMusicViewModel.activeInstance
        val track = vm?.currentTrackA?.value ?: vm?.currentTrack?.value
        val isPlaying = vm?.isPlayingA?.value == true || vm?.isPlaying?.value == true
        val bpm = vm?.bpmA?.value ?: 128

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, track, isPlaying, bpm)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            track: LocalTrack?,
            isPlaying: Boolean,
            bpm: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_player)

            // Track metadata
            val title = track?.title ?: "Kein Track geladen"
            val artistAndDeck = if (track != null) {
                "${track.artist} • DECK A ($bpm BPM)"
            } else {
                "Pioneer DJ Deck A • Bereit"
            }
            views.setTextViewText(R.id.widget_track_title, title)
            views.setTextViewText(R.id.widget_track_artist, artistAndDeck)

            // Play/Pause button icon
            val playPauseRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            views.setImageViewResource(R.id.widget_btn_play_pause, playPauseRes)

            // Open app on clicking track info or album art
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                100,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_info_container, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_album_art, openAppPendingIntent)

            // Action: Previous
            val prevIntent = Intent(context, MediaNotificationReceiver::class.java).apply {
                action = MediaNotificationManager.ACTION_PREV
            }
            val prevPending = PendingIntent.getBroadcast(
                context,
                101,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPending)

            // Action: Play/Pause
            val playPauseIntent = Intent(context, MediaNotificationReceiver::class.java).apply {
                action = MediaNotificationManager.ACTION_PLAY_PAUSE
            }
            val playPausePending = PendingIntent.getBroadcast(
                context,
                102,
                playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_play_pause, playPausePending)

            // Action: Next
            val nextIntent = Intent(context, MediaNotificationReceiver::class.java).apply {
                action = MediaNotificationManager.ACTION_NEXT
            }
            val nextPending = PendingIntent.getBroadcast(
                context,
                103,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next, nextPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context, track: LocalTrack?, isPlaying: Boolean, bpm: Int) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, DjQuickPlayerWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id, track, isPlaying, bpm)
            }
        }
    }
}
