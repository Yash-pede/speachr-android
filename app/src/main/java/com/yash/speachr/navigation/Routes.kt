package com.yash.speachr.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes : NavKey {

    @Serializable
    data object Home : NavKey

    @Serializable
    data object History : NavKey

    @Serializable
    data object Settings : NavKey

    @Serializable
    data object Paywall : NavKey

}