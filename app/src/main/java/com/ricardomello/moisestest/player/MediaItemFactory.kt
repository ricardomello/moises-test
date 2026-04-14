package com.ricardomello.moisestest.player

import androidx.media3.common.MediaItem

fun interface MediaItemFactory {
    fun fromUrl(url: String): MediaItem
}

