package com.yash.speachr.navigation

import androidx.annotation.DrawableRes
import com.yash.speachr.R

data class BottomNavItem(
    @DrawableRes val iconRes: Int,
    val title: String
)

val TOP_LEVEL_DESTINATIONS = mapOf(
    Routes.Home to BottomNavItem(
        iconRes = R.drawable.home_24px,
        title = "Home"
    ),
    Routes.History to BottomNavItem(
        iconRes = R.drawable.history_24px,
        title = "History"
    ),
    Routes.Settings to BottomNavItem(
        iconRes = R.drawable.settings_24px,
        title = "Settings"
    )

)