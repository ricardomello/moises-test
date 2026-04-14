package com.ricardomello.moisestest.ui.theme

import androidx.compose.ui.graphics.Color

private val placeholderColors = listOf(SongPurple, SongRed, SongBlue, SongPink, SongAmber)

fun placeholderColor(id: Int): Color = placeholderColors[id % placeholderColors.size]
