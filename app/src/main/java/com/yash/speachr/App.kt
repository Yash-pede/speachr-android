package com.yash.speachr

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yash.speachr.core.auth.AuthState
import com.yash.speachr.core.auth.AuthViewModel
import com.yash.speachr.core.permissions.PermissionViewModel
import com.yash.speachr.navigation.AppNavigation
import com.yash.speachr.ui.screens.onboarding.OnboardingScreen
import com.yash.speachr.ui.screens.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SpeachrApp(
    authViewModel: AuthViewModel = koinViewModel(),
    permissionViewModel: PermissionViewModel = koinViewModel()
) {
    val authState = authViewModel.authState.collectAsStateWithLifecycle().value
    val micGranted = permissionViewModel.micGranted.collectAsStateWithLifecycle().value
    val accessibilityGranted =
        permissionViewModel.accessibilityGranted.collectAsStateWithLifecycle().value

    val overlayGranted = permissionViewModel.overlayGranted.collectAsStateWithLifecycle().value
    val batteryOptimizationGranted =
        permissionViewModel.batteryIgnored.collectAsStateWithLifecycle().value

    val permissionsGranted = micGranted && accessibilityGranted && overlayGranted

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
            if (!permissionsGranted) {
                OnboardingScreen(
                    isAlreadyAuthenticated = true,
                    onOnboardingComplete = {

                    },
                    forceStep = 4, // Permissions step
                    initialPermissionStep = if (!micGranted) 0
                    else if (!overlayGranted) 1
                    else if(!batteryOptimizationGranted) 2
                    else if (!accessibilityGranted) 3
                    else 0
                )
            } else {
                AppNavigation()
            }
        }
    }
}
