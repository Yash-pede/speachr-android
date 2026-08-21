package com.yash.speachr.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.yash.speachr.ui.screens.onboarding.sections.HowItWorksScreen
import com.yash.speachr.ui.screens.onboarding.sections.LoginOnboarding
import com.yash.speachr.ui.screens.onboarding.sections.OnboardingInfoScreen
import com.yash.speachr.ui.screens.onboarding.sections.PermissionsOnboardingScreen
import com.yash.speachr.ui.screens.onboarding.sections.SetupPreferencesScreen

@Composable
fun OnboadringScreen() {

    val steps = OnboardingStep.all

    var currentIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    fun nextStep() {
        if (currentIndex < steps.lastIndex) {
            currentIndex++
        }
    }

    val currentStep = steps[currentIndex]

    when (currentStep) {

        OnboardingStep.Welcome -> {
            LoginOnboarding(
                onClick = {
                    nextStep()
                }
            )
        }

        OnboardingStep.InfoScreen -> {
            OnboardingInfoScreen(
                onNextClick = {
                    nextStep()
                }
            )
        }

        OnboardingStep.HowItWorksScreen -> {
            HowItWorksScreen(
                onFinish = {
                    nextStep()
                }
            )
        }

        OnboardingStep.SetupPreferences -> {
            SetupPreferencesScreen(
                onFinish = {
                    nextStep()
                }
            )
        }

        OnboardingStep.PermissionsOnboarding -> {
            PermissionsOnboardingScreen(
                onFinish = {
                    nextStep()
                }
            )
        }
    }
}