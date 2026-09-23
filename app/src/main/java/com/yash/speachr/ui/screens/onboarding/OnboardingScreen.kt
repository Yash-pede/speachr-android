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

/**
 * Hosts the first-run wizard.
 *
 * Two modes:
 *  - **Wizard** (default): the full Welcome → Info → How it works → Preferences → Permissions flow.
 *  - **Permission gate** ([permissionGateOnly] = true): only the permissions step, for users who
 *    already finished onboarding but are missing a permission. This prevents the wizard from
 *    replaying from the start every time they return from system settings.
 */
@Composable
fun OnboardingScreen(
    isAlreadyAuthenticated: Boolean = false,
    onOnboardingComplete: () -> Unit = {},
    forceStep: Int? = null,
    initialPermissionStep: Int = 0,
    permissionGateOnly: Boolean = false,
    authViewModel: AuthViewModel = koinViewModel()
) {

    // --- Permission gate: no wizard, no state machine, no persisted-step writes ---
    if (permissionGateOnly) {
        PermissionsOnboardingScreen(
            initialPage = initialPermissionStep,
            onFinish = onOnboardingComplete
        )
        return
    }

    val steps = OnboardingStep.all
    val persistedStep = authViewModel.getOnboardingStep()

    var currentIndex by rememberSaveable {
        mutableIntStateOf(forceStep ?: persistedStep)
    }

    // Update currentIndex if forceStep changes
    LaunchedEffect(forceStep) {
        forceStep?.let { currentIndex = it }
    }

    // Persist progress, but never let it regress — returning from a permission prompt used to
    // reset the saved step and force the user to walk through every screen again.
    LaunchedEffect(currentIndex) {
        if (currentIndex > persistedStep) {
            authViewModel.updateOnboardingStep(currentIndex)
        }
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
                initialPage = initialPermissionStep,
                onFinish = {
                    nextStep()
                }
            )
        }
    }
}
