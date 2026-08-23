package com.yash.speachr.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.yash.speachr.core.auth.AuthViewModel
import com.yash.speachr.ui.screens.onboarding.sections.HowItWorksScreen
import com.yash.speachr.ui.screens.onboarding.sections.LoginOnboarding
import com.yash.speachr.ui.screens.onboarding.sections.OnboardingInfoScreen
import com.yash.speachr.ui.screens.onboarding.sections.PermissionsOnboardingScreen
import com.yash.speachr.ui.screens.onboarding.sections.SetupPreferencesScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    isAlreadyAuthenticated: Boolean = false,
    onOnboardingComplete: () -> Unit = {},
    forceStep: Int? = null,
    authViewModel: AuthViewModel = koinViewModel()
) {

    val steps = OnboardingStep.all

    var currentIndex by rememberSaveable {
        mutableIntStateOf(forceStep ?: authViewModel.getOnboardingStep())
    }

    // Update currentIndex if forceStep changes
    LaunchedEffect(forceStep) {
        forceStep?.let { currentIndex = it }
    }

    // Save step whenever it changes
    LaunchedEffect(currentIndex) {
        authViewModel.updateOnboardingStep(currentIndex)
    }

    // Auto-advance if we just authenticated
    LaunchedEffect(isAlreadyAuthenticated) {
        if (isAlreadyAuthenticated && currentIndex == 0) {
            currentIndex = 1
        }
    }

    fun nextStep() {
        if (currentIndex < steps.lastIndex) {
            currentIndex++
        } else {
            onOnboardingComplete()
        }
    }

    val currentStep = steps[currentIndex]

    when (currentStep) {

        OnboardingStep.Welcome -> {
            LoginOnboarding()
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
