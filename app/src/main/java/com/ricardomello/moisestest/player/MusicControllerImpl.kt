package com.ricardomello.moisestest.player

import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicControllerImpl(
    private val controllerFuture: ListenableFuture<MediaController>,
    private val mediaItemFactory: MediaItemFactory,
    shouldPollProgress: Boolean = true,
) : MusicController {

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    override val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private var currentUrl: String? = null
    private var controller: MediaController? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val ctrl = controller ?: return
            if (playbackState == Player.STATE_READY) {
                _durationMs.value = ctrl.duration.coerceAtLeast(0L)
            }
        }
    }

    init {
        controllerFuture.addListener({
            val ctrl = controllerFuture.get()
            controller = ctrl
            ctrl.addListener(listener)
            val pending = currentUrl
            if (pending != null) {
                ctrl.setMediaItem(mediaItemFactory.fromUrl(pending))
                ctrl.prepare()
                ctrl.play()
            }
        }, { runnable -> runnable.run() })

        if (shouldPollProgress) {
            scope.launch {
                while (true) {
                    delay(1000)
                    if (_isPlaying.value) {
                        val ctrl = controller ?: continue
                        _positionMs.value = ctrl.currentPosition.coerceAtLeast(0L)
                        _durationMs.value = ctrl.duration.coerceAtLeast(0L)
                    }
                }
            }
        }
    }

    override fun load(url: String) {
        if (url == currentUrl) {
            return
        }

        currentUrl = url
        val ctrl = controller ?: return
        ctrl.setMediaItem(mediaItemFactory.fromUrl(url))
        ctrl.prepare()
        ctrl.play()
    }

    override fun play() {
        controller?.play()
    }

    override fun pause() {
        controller?.pause()
    }

    override fun seekTo(ms: Long) {
        controller?.seekTo(ms)
    }

    override fun setRepeat(on: Boolean) {
        controller?.repeatMode = if (on) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    override fun release() {
        scope.cancel()
    }
}
