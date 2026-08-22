package com.yash.speachr

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yash.speachr.core.auth.AuthState
import com.yash.speachr.core.auth.AuthViewModel
import com.yash.speachr.navigation.AppNavigation
import com.yash.speachr.ui.screens.onboarding.OnboardingScreen
import com.yash.speachr.ui.screens.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SpeachrApp(
    authViewModel: AuthViewModel = koinViewModel()
) {
    val authState = authViewModel.authState.collectAsStateWithLifecycle().value

    when (authState) {
        AuthState.Loading -> {
            SplashScreen()
        }

        AuthState.Unauthenticated, AuthState.Onboarding -> {
            OnboardingScreen(
                isAlreadyAuthenticated = authState == AuthState.Onboarding,
                onOnboardingComplete = {
                    authViewModel.completeOnboarding()
                }
            )
        }

        AuthState.Authenticated -> {
            AppNavigation()
        }
    }
}
