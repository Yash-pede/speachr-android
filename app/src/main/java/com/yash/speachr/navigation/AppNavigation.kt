package com.yash.speachr.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Routes.Home)
    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomNavigationBar(
                selectedKey = backStack.last(),
                onSelectKey = { key ->
                    backStack.add(key)
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            backStack = backStack,
            onBack = {
                backStack.removeLastOrNull()
            },
            entryProvider = entryProvider {
                entry<Routes.Home> {
                    Text(
                        text = "HOME",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                entry<Routes.Style> {
                    Text(
                        text = "Style",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                entry<Routes.Flow> {
                    Text(
                        text = "Flow",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                entry<Routes.Settings> {
                    Text(
                        text = "Settings",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            }
        )
    }

}