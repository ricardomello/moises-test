package com.ricardomello.moisestest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent

import com.ricardomello.moisestest.ui.theme.MusicSubdued
import com.ricardomello.moisestest.ui.theme.placeholderColor

@Composable
fun SongArtwork(
    artworkUrl: String?,
    songId: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
) {
    val shape = RoundedCornerShape(cornerRadius)

    if (artworkUrl == null) {
        ArtworkPlaceholder(
            songId = songId,
            size = size,
            cornerRadius = cornerRadius,
            modifier = modifier,
        )
    } else {
        SubcomposeAsyncImage(
            model = artworkUrl.toHighRes(size.value.toInt()),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(shape),
        ) {
            val state by painter.state.collectAsState()
            if (state is AsyncImagePainter.State.Success) {
                SubcomposeAsyncImageContent()
            } else {
                ArtworkPlaceholder(songId = songId, size = size, cornerRadius = cornerRadius)
            }
        }
    }
}

@Composable
private fun ArtworkPlaceholder(
    songId: Int,
    size: Dp,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(placeholderColor(songId)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(size * 0.45f),
        )
    }
}

private fun String.toHighRes(targetSizePx: Int): String {
    val sizeTag = "${targetSizePx}x${targetSizePx}bb"
    return replace(Regex("""\d+x\d+bb"""), sizeTag)
}
