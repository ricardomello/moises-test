package com.ricardomello.moisestest.home.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItunesSearchResponse(
    val resultCount: Int,
    val results: List<ItunesSearchResult>,
)
