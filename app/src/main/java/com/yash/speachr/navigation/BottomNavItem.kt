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
    Routes.Style to BottomNavItem(
        iconRes = R.drawable.brush_24px,
        title = "Style"
    ),
    Routes.Flow to BottomNavItem(
        iconRes = R.drawable.motion_mode_24px,
        title = "Flow"
    ),
    Routes.Settings to BottomNavItem(
        iconRes = R.drawable.settings_24px,
        title = "Settings"
    )

)