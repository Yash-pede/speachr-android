package com.yash.speachr.ui.components

import androidx.annotation.DrawableRes
import com.yash.speachr.R

/**
 * Single source of truth for the permissions Speachr asks for: display name, the short
 * "why we need this" explanation shown to the user, and whether it is strictly required.
 *
 * The rationale copy intentionally mirrors the onboarding explanations so the user reads the
 * same reason in both places.
 */
enum class AppPermission(
    @DrawableRes val iconRes: Int,
    val title: String,
    val rationale: String,
    val optional: Boolean = false,
) {
    MICROPHONE(
        iconRes = R.drawable.mic_24px,
        title = "Microphone",
        rationale = "Speachr needs to hear your voice to transcribe it. Your audio is never stored.",
    ),
    OVERLAY(
        iconRes = R.drawable.settings_voice_24px,
        title = "Display over other apps",
        rationale = "Lets the Speachr bubble float above your keyboard and other apps.",
    ),
    ACCESSIBILITY(
        iconRes = R.drawable.grain_24px,
        title = "Accessibility service",
        rationale = "Reads the text field you're in and inserts your dictation automatically.",
    ),
    BATTERY(
        iconRes = R.drawable.battery_full_24px,
        title = "Ignore battery optimisation",
        rationale = "Stops your phone from pausing Speachr mid-dictation when it runs in the background.",
        optional = true,
    ),
}
