package com.livlify.app.playback

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.livlify.app.data.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerUiState(
    val hasMedia: Boolean = false,
    val currentSongId: Long? = null,
    val title: String = "",
    val artist: String = "",
    val artUri: Uri? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)

/** Talks to PlaybackService and exposes what the UI needs. */
class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var pending: Pair<List<Song>, Int>? = null
    private var releaseController: () -> Unit = {}

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            refresh(player)
        }
    }

    init {
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        releaseController = { MediaController.releaseFuture(future) }

        future.addListener({
            val c = runCatching { future.get() }.getOrNull()
            if (c != null) {
                controller = c
                c.addListener(listener)
                refresh(c)
                pending?.let { (songs, index) -> start(c, songs, index) }
                pending = null
            }
        }, ContextCompat.getMainExecutor(app))

        // Moves the seek bar while a song is playing
        viewModelScope.launch {
            while (true) {
                delay(400)
                val c = controller
                if (c != null && c.isPlaying) {
                    _state.update {
                        it.copy(
                            positionMs = c.currentPosition.coerceAtLeast(0L),
                            durationMs = c.duration.takeIf { d -> d != C.TIME_UNSET } ?: it.durationMs
                        )
                    }
                }
            }
        }
    }

    private fun refresh(player: Player) {
        val item = player.currentMediaItem
        val meta = item?.mediaMetadata
        _state.value = PlayerUiState(
            hasMedia = item != null,
            currentSongId = item?.mediaId?.toLongOrNull(),
            title = meta?.title?.toString().orEmpty(),
            artist = meta?.artist?.toString().orEmpty(),
            artUri = meta?.artworkUri,
            isPlaying = player.isPlaying ||
                (player.playWhenReady && player.playbackState == Player.STATE_BUFFERING),
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L,
            shuffle = player.shuffleModeEnabled,
            repeatMode = player.repeatMode
        )
    }

    private fun start(c: MediaController, songs: List<Song>, index: Int) {
        c.setMediaItems(songs.map { it.toMediaItem() }, index, 0L)
        c.prepare()
        c.play()
    }

    /** Called when a song is tapped in a list. The whole list becomes the queue. */
    fun onSongClicked(songs: List<Song>, index: Int) {
        val song = songs.getOrNull(index) ?: return
        val c = controller
        if (c == null) {
            pending = songs to index
            return
        }
        if (_state.value.hasMedia && _state.value.currentSongId == song.id) {
            togglePlayPause()
        } else {
            start(c, songs, index)
        }
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_ENDED) c.seekToDefaultPosition(0)
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }

    fun next() {
        controller?.seekToNext()
    }

    fun previous() {
        // Restarts the song if it's past 3 seconds, otherwise goes to the previous one
        controller?.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        val c = controller ?: return
        c.seekTo(positionMs)
        _state.update { it.copy(positionMs = positionMs) }
    }

    fun toggleShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    fun cycleRepeat() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    override fun onCleared() {
        controller?.removeListener(listener)
        releaseController()
        controller = null
        super.onCleared()
    }
}

private fun Song.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(artUri)
                .build()
        )
        .build()
