package com.yash.speachr.core.floating

import android.app.Application
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yash.speachr.core.database.DictationDao
import com.yash.speachr.core.database.DictationEntity
import com.yash.speachr.core.model.DEFAULT_LANGUAGE_NAME
import com.yash.speachr.core.model.ToneStrategy
import com.yash.speachr.core.repository.AudioRepository
import com.yash.speachr.services.SpeachrPasteAccessibilityService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import android.content.Context

class FloatingViewModel(
    application: Application,
    private val audioRepository: AudioRepository,
    private val dictationDao: DictationDao
) : AndroidViewModel(application) {

    var isRecording by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    private var audioFile: File? = null
    private var mediaRecorder: MediaRecorder? = null

    private var transcriptionJob: Job? = null

    /**
     * Bumped every time a transcription starts or is cancelled. The in-flight coroutine only
     * touches [isLoading] while its token is still current, so cancelling and immediately
     * starting a new recording can't clobber the newer request's state.
     */
    private var transcriptionToken = 0

    fun toggleRecording() {
        if (isLoading) return
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    /**
     * Drops the in-flight transcription request. Nothing is pasted into the target app and
     * nothing is saved to history — the bubble just falls back to its idle state.
     */
    fun cancelTranscription() {
        if (!isLoading) return

        Log.d("FloatingVM", "Transcription cancelled by user")
        transcriptionToken++
        transcriptionJob?.cancel()
        transcriptionJob = null
        isLoading = false
    }

    private var recordingStartTime: Long = 0

    private fun startRecording() {
        if (isRecording) return
        
        // Check for permission first
        val permission = android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.RECORD_AUDIO
        ) == permission

        if (!hasPermission) {
            Log.e("FloatingVM", "Microphone permission NOT granted!")
            SpeachrPasteAccessibilityService.pasteText("🚫 Mic Permission Required")
            return
        }

        Log.d("FloatingVM", "Recording Started")
        try {
            val cacheDir = getApplication<Application>().externalCacheDir
            audioFile = File(cacheDir, "recording-${Instant.now().epochSecond}.m4a")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(getApplication())
            } else {
                @Suppress("DEPRECATION") MediaRecorder()
            }.apply {
                // Using MIC as it's the most compatible
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(64000)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            recordingStartTime = System.currentTimeMillis()
            isRecording = true
        } catch (e: Exception) {
            Log.e("FloatingVM", "MediaRecorder start failed", e)
            isRecording = false
        }
    }

    private fun stopRecording() {
        if (!isRecording) return
        
        isRecording = false
        Log.d("FloatingVM", "Recording Stopped")

        val duration = (System.currentTimeMillis() - recordingStartTime) / 1000

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            audioFile?.let { file ->
                Log.d("FloatingVM", "File saved: ${file.absolutePath}, size: ${file.length()} bytes")
                val token = ++transcriptionToken
                transcriptionJob = viewModelScope.launch {
                    isLoading = true
                    val sharedPrefs = getApplication<Application>().getSharedPreferences("user_settings", Context.MODE_PRIVATE)
                    val toneStrategy = sharedPrefs.getString("tone", ToneStrategy.AUTO.name)
                    val manualTone = sharedPrefs.getString("manualtone", "PROFESSIONAL")
                    val targetLanguage =
                        sharedPrefs.getString("language", DEFAULT_LANGUAGE_NAME)
                            ?: DEFAULT_LANGUAGE_NAME
                    
                    val toneToSend = if (toneStrategy == ToneStrategy.GLOBAL.name) {
                        manualTone?.lowercase() ?: "professional"
                    } else {
                        "auto"
                    }

                    try {
                        val result = audioRepository.transcribeAudio(file, toneToSend, targetLanguage)

                        // The user may have tapped stop while we were waiting. Bail out before
                        // touching the target app, so a cancelled request never pastes anything.
                        if (token != transcriptionToken) {
                            Log.d("FloatingVM", "Transcription discarded (request cancelled)")
                            return@launch
                        }

                        if (result != null) {
                            Log.d("FloatingVM", "Transcription: ${result.text}")
                            SpeachrPasteAccessibilityService.pasteText(result.text)
                            
                            // Save to local DB
                            dictationDao.insert(
                                DictationEntity(
                                    text = result.text,
                                    timestamp = System.currentTimeMillis(),
                                    wordCount = result.text.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size,
                                    durationSeconds = duration
                                )
                            )
                        } else {
                            Log.e("FloatingVM", "Transcription failed")
                            SpeachrPasteAccessibilityService.pasteText("😞 Error")
                        }
                    } catch (e: CancellationException) {
                        // User tapped the stop icon: drop the request silently.
                        Log.d("FloatingVM", "Transcription request dropped")
                    } catch (e: Exception) {
                        Log.e("FloatingVM", "Error during transcription", e)
                        SpeachrPasteAccessibilityService.pasteText("😞 Error")
                    } finally {
                        // Only the newest request may reset shared state.
                        if (token == transcriptionToken) {
                            isLoading = false
                            transcriptionJob = null
                        }
                        // Clean up file after upload attempt
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FloatingVM", "MediaRecorder stop failed", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}
