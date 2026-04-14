package com.ricardomello.moisestest.shared.data.remote

data class RemoteSong(
    val id: Long,
    val title: String,
    val artist: String,
    val albumId: Long,
    val albumName: String?,
    val artworkUrl: String?,
    val previewUrl: String?,
    val genre: String?,
    val durationMs: Long?,
    val releaseDate: String?,
    val trackNumber: Int?,
)
