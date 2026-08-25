package com.yash.speachr.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
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
    val currentKey = backStack.last()
    
    val showBottomBar = currentKey != Routes.Paywall

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    selectedKey = currentKey,
                    onSelectKey = { key ->
                        // Clear backstack when switching tabs to avoid deep stacks
                        if (key != currentKey) {
                            backStack.clear()
                            backStack.add(key)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showBottomBar) innerPadding else PaddingValues(0.dp)),
            backStack = backStack,
            onBack = {
                backStack.removeLastOrNull()
            },
            entryProvider = entryProvider {
                entry<Routes.Home> {
                    HomeScreen()
                }

                entry<Routes.History> {
                    HistoryScreen()
                }

                entry<Routes.Settings> {
                    SettingsScreen(onNavigateToPaywall = {
                        backStack.add(Routes.Paywall)
                    })
                }

                entry<Routes.Paywall> {
                    Paywall(
                        options = PaywallOptions.Builder(
                            dismissRequest = { backStack.removeLastOrNull() }
                        ).setListener(object : PaywallListener {
                            override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: StoreTransaction) {
                                backStack.removeLastOrNull()
                            }
                            override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                                backStack.removeLastOrNull()
                            }
                        }).build()
                    )
                }
            }
        )
    }
}