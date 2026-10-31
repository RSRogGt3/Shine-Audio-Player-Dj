package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ui.viewmodels.LocalMusicViewModel

class MediaNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            MediaNotificationManager.ACTION_PLAY_PAUSE -> {
                LocalMusicViewModel.togglePlayPauseGlobal(context)
            }
            MediaNotificationManager.ACTION_NEXT -> {
                LocalMusicViewModel.playNextGlobal(context)
            }
            MediaNotificationManager.ACTION_PREV -> {
                LocalMusicViewModel.playPreviousGlobal(context)
            }
            MediaNotificationManager.ACTION_SYNC -> {
                LocalMusicViewModel.syncGlobal(context)
            }
            MediaNotificationManager.ACTION_PLAY_A -> {
                LocalMusicViewModel.togglePlayPauseAGlobal(context)
            }
            MediaNotificationManager.ACTION_PLAY_B -> {
                LocalMusicViewModel.togglePlayPauseBGlobal(context)
            }
            MediaNotificationManager.ACTION_CUE_A -> {
                LocalMusicViewModel.cueDeckAGlobal(context)
            }
        }
    }
}
