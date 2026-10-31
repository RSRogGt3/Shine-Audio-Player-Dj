package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Music Creator", appName)
    assertEquals("DJ Quick Player", context.getString(R.string.widget_quick_player_name))
    assertEquals("DJ Dual-Deck Controller", context.getString(R.string.widget_dual_deck_name))
  }

  @Test
  fun `test notification channel creation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.notification.MediaNotificationManager.createNotificationChannel(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val channel = notificationManager.getNotificationChannel(com.example.notification.MediaNotificationManager.CHANNEL_ID)
    org.junit.Assert.assertNotNull(channel)
    assertEquals("DJ Musik-Wiedergabe", channel.name)
  }

  @Test
  fun `test notification manager does not crash on show update`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.notification.MediaNotificationManager.showOrUpdateNotification(
      context = context,
      trackA = null,
      trackB = null,
      isPlayingA = false,
      isPlayingB = false,
      bpmA = 128,
      bpmB = 128,
      isSync = false
    )
    com.example.notification.MediaNotificationManager.cancelNotification(context)
  }

  @Test
  fun `test streaming providers and catalog`() {
    val catalog = com.example.ui.models.DefaultStreamingCatalog.tracks
    org.junit.Assert.assertTrue("Catalog should not be empty", catalog.isNotEmpty())

    val spotifyTracks = catalog.filter { it.provider == com.example.ui.models.StreamingProvider.SPOTIFY }
    val ytmTracks = catalog.filter { it.provider == com.example.ui.models.StreamingProvider.YOUTUBE_MUSIC }
    val amzTracks = catalog.filter { it.provider == com.example.ui.models.StreamingProvider.AMAZON_MUSIC }

    org.junit.Assert.assertTrue("Should have Spotify tracks", spotifyTracks.isNotEmpty())
    org.junit.Assert.assertTrue("Should have YouTube Music tracks", ytmTracks.isNotEmpty())
    org.junit.Assert.assertTrue("Should have Amazon Music tracks", amzTracks.isNotEmpty())

    val first = catalog.first()
    val local = first.toLocalTrack(-999L)
    assertEquals(first.title, local.title)
    assertEquals(-999L, local.id)
    org.junit.Assert.assertTrue("Artist should include provider label", local.artist.contains(first.provider.displayName))
    org.junit.Assert.assertFalse("Audio stream should not be video", local.isVideo)
  }

  @Test
  fun `test player notification types`() {
    val notif = com.example.ui.models.PlayerNotification(
      title = "BEAT SYNC",
      message = "Deck B auf 128 BPM angeglichen",
      type = com.example.ui.models.PlayerNotificationType.SYNC
    )
    assertEquals("BEAT SYNC", notif.title)
    assertEquals(com.example.ui.models.PlayerNotificationType.SYNC, notif.type)
    assertEquals("BEAT SYNC", notif.type.label)
    assertEquals(0xFFFFD600, notif.type.colorHex)
  }

  @Test
  fun `test streaming connection toggles`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app = context as android.app.Application
    val vm = com.example.ui.viewmodels.LocalMusicViewModel(app)

    org.junit.Assert.assertTrue("Spotify should be connected by default", vm.isSpotifyConnected.value)
    org.junit.Assert.assertTrue("YouTube Music should be connected by default", vm.isYouTubeMusicConnected.value)
    org.junit.Assert.assertTrue("Amazon Music should be connected by default", vm.isAmazonMusicConnected.value)

    vm.toggleSpotifyConnection()
    org.junit.Assert.assertFalse("Spotify should be disconnected after toggle", vm.isSpotifyConnected.value)
    org.junit.Assert.assertFalse("isProviderConnected should return false", vm.isProviderConnected(com.example.ui.models.StreamingProvider.SPOTIFY))

    vm.toggleSpotifyConnection()
    org.junit.Assert.assertTrue("Spotify should be reconnected", vm.isSpotifyConnected.value)
  }
}
