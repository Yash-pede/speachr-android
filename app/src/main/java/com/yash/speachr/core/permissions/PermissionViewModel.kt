package com.yash.speachr.core.permissions

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionViewModel(application: Application) : AndroidViewModel(application) {
    private val _micGranted = MutableStateFlow(false)
    val micGranted = _micGranted.asStateFlow()

    private val _overlayGranted = MutableStateFlow(false)
    val overlayGranted = _overlayGranted.asStateFlow()

    private val _batteryIgnored = MutableStateFlow(false)
    val batteryIgnored = _batteryIgnored.asStateFlow()

    private val _accessibilityGranted = MutableStateFlow(false)
    val accessibilityGranted = _accessibilityGranted.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        _micGranted.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        _overlayGranted.value = Settings.canDrawOverlays(context)
        
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        _batteryIgnored.value = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        
        _accessibilityGranted.value = isAccessibilityServiceEnabled(context)
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = "${context.packageName}/com.yash.speachr.services.SpeachrAccessibilityService"
        val settingValue = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return settingValue?.contains(expectedComponentName) == true
    }

    fun openMicSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openOverlaySettings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openBatterySettings(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
