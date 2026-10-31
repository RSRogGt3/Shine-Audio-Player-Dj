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

class DjDualDeckWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val vm = LocalMusicViewModel.activeInstance
        val trackA = vm?.currentTrackA?.value
        val trackB = vm?.currentTrackB?.value
        val isPlayingA = vm?.isPlayingA?.value == true
        val isPlayingB = vm?.isPlayingB?.value == true
        val bpmA = vm?.bpmA?.value ?: 128
        val bpmB = vm?.bpmB?.value ?: 128

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, trackA, trackB, isPlayingA, isPlayingB, bpmA, bpmB)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            trackA: LocalTrack?,
            trackB: LocalTrack?,
            isPlayingA: Boolean,
            isPlayingB: Boolean,
            bpmA: Int,
            bpmB: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_dual_deck)

            // Deck A Info
            views.setTextViewText(R.id.widget_deck_a_title, trackA?.title ?: "Deck A Bereit")
            views.setTextViewText(R.id.widget_deck_a_bpm, "$bpmA BPM")
            val playResA = if (isPlayingA) R.drawable.ic_pause else R.drawable.ic_play
            views.setImageViewResource(R.id.widget_btn_play_a, playResA)

            // Deck B Info
            views.setTextViewText(R.id.widget_deck_b_title, trackB?.title ?: "Deck B Bereit")
            views.setTextViewText(R.id.widget_deck_b_bpm, "$bpmB BPM")
            val playResB = if (isPlayingB) R.drawable.ic_pause else R.drawable.ic_play
            views.setImageViewResource(R.id.widget_btn_play_b, playResB)

            // Open app on clicking background
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                200,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_dual_deck_root, openAppPendingIntent)

            // Action: Play / Pause Deck A
            val playAIntent = Intent(context, MediaNotificationReceiver::class.java).apply {
                action = MediaNotificationManager.ACTION_PLAY_A
            }
            val playAPending = PendingIntent.getBroadcast(
                context,
                201,
                playAIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_play_a, playAPending)

            // Action: Play / Pause Deck B
            val playBIntent = Intent(context, MediaNotificationReceiver::class.java).apply {
                action = MediaNotificationManager.ACTION_PLAY_B
            }
            val playBPending = PendingIntent.getBroadcast(
                context,
                202,
                playBIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_play_b, playBPending)

            // Action: CUE A
            val cueAIntent = Intent(context, MediaNotificationReceiver::class.java).apply {
                action = MediaNotificationManager.ACTION_CUE_A
            }
            val cueAPending = PendingIntent.getBroadcast(
                context,
                203,
                cueAIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_cue_a, cueAPending)

            // Action: SYNC Decks
            val syncIntent = Intent(context, MediaNotificationReceiver::class.java).apply {
                action = MediaNotificationManager.ACTION_SYNC
            }
            val syncPending = PendingIntent.getBroadcast(
                context,
                204,
                syncIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_sync, syncPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(
            context: Context,
            trackA: LocalTrack?,
            trackB: LocalTrack?,
            isPlayingA: Boolean,
            isPlayingB: Boolean,
            bpmA: Int,
            bpmB: Int
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, DjDualDeckWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id, trackA, trackB, isPlayingA, isPlayingB, bpmA, bpmB)
            }
        }
    }
}
