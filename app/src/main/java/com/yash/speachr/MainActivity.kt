package com.yash.speachr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yash.speachr.ui.theme.SpeachrTheme
import org.koin.androidx.compose.KoinAndroidContext

const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KoinAndroidContext {
                SpeachrTheme {
                    SpeachrApp()
                }
            }
        }
    }
}