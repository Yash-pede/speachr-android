package com.yash.speachr.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class SpeachrAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events
    }

    override fun onInterrupt() {
        // Handle interrupt
    }
}
