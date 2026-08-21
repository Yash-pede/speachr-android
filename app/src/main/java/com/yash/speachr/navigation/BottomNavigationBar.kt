package com.yash.speachr.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.NavKey

@Composable
fun BottomNavigationBar(
    selectedKey: NavKey,
    onSelectKey: (NavKey) -> Unit,
    moidifier: Modifier = Modifier
) {
    NavigationBar {
        TOP_LEVEL_DESTINATIONS.keys.forEach { topLevelDestination ->
            val navItem = TOP_LEVEL_DESTINATIONS[topLevelDestination]
            NavigationBarItem(
                selected = topLevelDestination == selectedKey,
                onClick = {
                    onSelectKey(topLevelDestination)
                },
                icon = {
                    Icon(
                        painter = painterResource(id = navItem!!.iconRes),
                        contentDescription = navItem.title
                    )
                },
                label = { Text(text = navItem!!.title) }
            )
        }
    }
}