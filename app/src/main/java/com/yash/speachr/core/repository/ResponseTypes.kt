package com.yash.speachr.core.repository

import kotlinx.serialization.Serializable

@Serializable
data class AudioTranscribeApiResponse(
    val text: String = "",
    val sourceLanguage: String? = null,
    val resultingLanguage: String? = null,
)
