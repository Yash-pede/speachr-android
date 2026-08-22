package com.yash.speachr.core.auth

sealed interface AuthState {

    data object Loading : AuthState

    data object Authenticated : AuthState

    data object Unauthenticated : AuthState

    data object Onboarding : AuthState
}
