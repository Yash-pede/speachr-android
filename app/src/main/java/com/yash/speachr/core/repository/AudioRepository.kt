package com.yash.speachr.core.repository

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import java.io.File

class AudioRepository(private val client: HttpClient) {

    suspend fun transcribeAudio(file: File, tone: String, targetLanguage: String): AudioTranscribeApiResponse? {
        Log.d(
            "AudioRepository",
            "Attempting to upload file: ${file.absolutePath} size: ${file.length()} with tone: $tone and targetLanguage: $targetLanguage"
        )
        return try {
            val response = client.post("/audio/transcribe") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                                append(HttpHeaders.ContentType, "audio/mp4")
                            })
                            append("tone", tone)
                            append("target_language", targetLanguage)
                        }
                    )
                )
            }
            
            val rawBody = response.bodyAsText()
            Log.d("AudioRepository", "Response status: ${response.status}")
            Log.d("AudioRepository", "RAW RESPONSE: $rawBody")

            if (response.status.isSuccess()) {
                val json = Json { ignoreUnknownKeys = true }
                val body = json.decodeFromString<AudioTranscribeApiResponse>(rawBody)
                Log.d("AudioRepository", "Parsed body: $body")
                body
            } else {
                Log.e("AudioRepository", "UPLOAD FAILED with status ${response.status}: $rawBody")
                null
            }
        } catch (e: CancellationException) {
            // Propagate cancellation so callers can tell "user aborted" apart from "request failed"
            // (swallowing it here would look like a null result and trigger the error path).
            Log.d("AudioRepository", "Transcription request cancelled")
            throw e
        } catch (e: Exception) {
            Log.e("AudioRepository", "TRANSCRIPTION ERROR", e)
            null
        }
    }
}
