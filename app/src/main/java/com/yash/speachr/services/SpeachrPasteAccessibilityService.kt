package com.yash.speachr.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.net.toUri

class SpeachrPasteAccessibilityService : AccessibilityService() {

    private val TAG = "PasteService"

    companion object {

        var instance: SpeachrPasteAccessibilityService? = null

        fun pasteText(text: String) {

            try {
                val rootNode = instance?.rootInActiveWindow ?: return

                val focusedInputNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

                if (focusedInputNode != null && focusedInputNode.isEditable) {
                    var existingText = focusedInputNode.text?.toString() ?: ""
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusedInputNode.isShowingHintText) {
                        existingText = ""
                    }
                    val cursorStart = focusedInputNode.textSelectionStart
                    val cursorEnd = focusedInputNode.textSelectionEnd

                    val combinedText =
                        if (cursorStart in 0..existingText.length && cursorEnd in 0..existingText.length) {
                            // If the cursor is valid, slice the text into two parts
                            val realStart = minOf(cursorStart, cursorEnd)
                            val realEnd = maxOf(cursorStart, cursorEnd)

                            val beforeCursor = existingText.substring(0, realStart)
                            val afterCursor = existingText.substring(realEnd)

                            // Sandwich our new text in the middle!
                            // (Adding a space before the new text if needed)
                            val space =
                                if (beforeCursor.isNotEmpty() && !beforeCursor.endsWith(" ")) " " else ""
                            "$beforeCursor$space$text$space$afterCursor"
                        } else {
                            // Fallback: If no cursor is found, just append to the end
                            if (existingText.isNotEmpty()) "$existingText $text" else text
                        }

                    val arguments = Bundle().apply {
                        putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            combinedText
                        )
                    }

                    val success = focusedInputNode.performAction(
                        AccessibilityNodeInfo.ACTION_SET_TEXT, arguments
                    )
                    Log.d("PasteService", "Paste attempt status: $success")
                }
            } catch (e: Exception) {
                Log.e("PasteService", "Error trying to paste text: ${e.message}")
            }
        }

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_FOCUSED, AccessibilityEvent.TYPE_VIEW_CLICKED, AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                checkIfTextBoxActive()
            }
        }
    }

    private fun checkIfTextBoxActive() {
        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            stopService()
            return
        }

        val focusedInputNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

        if (focusedInputNode != null && focusedInputNode.isEditable) {
            val serviceIntent = Intent(this, FloatingService::class.java)
            if (Settings.canDrawOverlays(this)) {
                startForegroundService(serviceIntent)
            } else {
                // Request SYSTEM_ALERT_WINDOW permission
                val overlayIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:$packageName".toUri()
                )
                startForegroundService(overlayIntent)
            }
        } else {
            stopService()
        }

    }

    private fun stopService() {
        stopService(Intent(this, FloatingService::class.java))
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service Interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        Log.d(TAG, "Speachr Accessibility Service Successfully Connected!")

        // Automatically bring the app back to front when service is enabled
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            launchIntent?.let {
                it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error returning to app: ${e.message}")
        }
    }
}
