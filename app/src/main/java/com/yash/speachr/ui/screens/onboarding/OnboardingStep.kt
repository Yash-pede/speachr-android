package com.yash.speachr.ui.screens.onboarding

sealed class OnboardingStep {

    data object Welcome : OnboardingStep()

    data object InfoScreen : OnboardingStep()

    data object HowItWorksScreen : OnboardingStep()

    data object SetupPreferences : OnboardingStep()

    data object PermissionsOnboarding : OnboardingStep()

    companion object {

        val all = listOf(
            Welcome,
            InfoScreen,
            HowItWorksScreen,
            SetupPreferences,
            PermissionsOnboarding
        )
    }
}