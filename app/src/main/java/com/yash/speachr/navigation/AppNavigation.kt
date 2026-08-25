package com.yash.speachr.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import com.yash.speachr.ui.screens.history.HistoryScreen
import com.yash.speachr.ui.screens.home.HomeScreen
import com.yash.speachr.ui.screens.settings.SettingsScreen

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Routes.Home)
    val currentKey = backStack.lastOrNull() ?: Routes.Home
    
    // UI configuration based on current destination
    val showBottomBar = currentKey != Routes.Paywall

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(backStack, currentKey)
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showBottomBar) innerPadding else PaddingValues(0.dp)),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<Routes.Home> { HomeScreen() }
                entry<Routes.History> { HistoryScreen() }
                entry<Routes.Settings> { 
                    SettingsScreen(onNavigateToPaywall = { backStack.add(Routes.Paywall) }) 
                }
                entry<Routes.Paywall> {
                    FullscreenPaywall(onDismiss = { 
                        if (backStack.lastOrNull() == Routes.Paywall) {
                            backStack.removeLastOrNull()
                        }
                    })
                }
            }
        )
    }
}

@Composable
private fun AppBottomBar(backStack: NavBackStack<NavKey>, currentKey: NavKey) {
    BottomNavigationBar(
        selectedKey = currentKey,
        onSelectKey = { key ->
            if (key != currentKey) {
                // Keep the navigation history clean by swapping the top level destination
                backStack.removeLastOrNull()
                backStack.add(key)
            }
        }
    )
}

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
private fun FullscreenPaywall(onDismiss: () -> Unit) {
    Paywall(
        options = PaywallOptions.Builder(dismissRequest = onDismiss)
            .setListener(object : PaywallListener {
                override fun onPurchaseCompleted(customerInfo: com.revenuecat.purchases.CustomerInfo, storeTransaction: com.revenuecat.purchases.models.StoreTransaction) {
                    onDismiss()
                }
                override fun onRestoreCompleted(customerInfo: com.revenuecat.purchases.CustomerInfo) {
                    onDismiss()
                }
            })
            .build()
    )
}
