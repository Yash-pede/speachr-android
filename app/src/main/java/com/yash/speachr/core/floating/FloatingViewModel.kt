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
import com.yash.speachr.core.model.ToneStrategy
import com.yash.speachr.core.repository.AudioRepository
import com.yash.speachr.services.SpeachrPasteAccessibilityService
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

    private var audioFile: File? = null
    private var mediaRecorder: MediaRecorder? = null

    fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private var recordingStartTime: Long = 0

    private fun startRecording() {
        if (isRecording) return
        
        Log.d("FloatingVM", "Recording Started")
        try {
            val cacheDir = getApplication<Application>().externalCacheDir
            audioFile = File(cacheDir, "recording-${Instant.now().epochSecond}.3gp")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(getApplication())
            } else {
                @Suppress("DEPRECATION") MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            recordingStartTime = System.currentTimeMillis()
            isRecording = true
        } catch (e: Exception) {
            Log.e("FloatingVM", "MediaRecorder prepare() failed", e)
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
                viewModelScope.launch {
                    val sharedPrefs = getApplication<Application>().getSharedPreferences("user_settings", Context.MODE_PRIVATE)
                    val toneStrategy = sharedPrefs.getString("tone", ToneStrategy.AUTO.name)
                    val manualTone = sharedPrefs.getString("manualtone", "PROFESSIONAL")
                    
                    val toneToSend = if (toneStrategy == ToneStrategy.GLOBAL.name) {
                        manualTone?.lowercase() ?: "professional"
                    } else {
                        "auto"
                    }

                    val result = audioRepository.transcribeAudio(file, toneToSend)
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
                    // Clean up file after upload attempt
                    if (file.exists()) {
                        file.delete()
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
