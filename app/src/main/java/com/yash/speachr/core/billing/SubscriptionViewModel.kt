package com.yash.speachr.core.billing

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener

class SubscriptionViewModel : ViewModel() {

    var isPro by mutableStateOf(false)
        private set

    private val customerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
        Log.d("SubscriptionVM", "CustomerInfo updated listener triggered")
        checkProStatus(customerInfo)
    }

    init {
        Log.d("SubscriptionVM", "Initializing SubscriptionViewModel")
        Purchases.sharedInstance.updatedCustomerInfoListener = customerInfoListener
        updateCustomerInfo()
    }

    fun updateCustomerInfo() {
        Log.d("SubscriptionVM", "Fetching customer info...")
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                Log.d("SubscriptionVM", "Customer info received successfully")
                checkProStatus(customerInfo)
            }

            override fun onError(error: PurchasesError) {
                Log.e("SubscriptionVM", "Error fetching customer info: ${error.message}")
            }
        })
    }

    private fun checkProStatus(customerInfo: CustomerInfo) {
        // Check for any active entitlement. 
        // We'll check for "pro" specifically, but also log all active ones for debugging.
        val activeEntitlements = customerInfo.entitlements.active.keys
        Log.d("SubscriptionVM", "Active entitlements: $activeEntitlements")
        
        // Use "pro" as the primary identifier, but if any entitlement is active, consider it Pro for now
        // or strictly "pro" if that's your configuration.
        isPro = customerInfo.entitlements["pro"]?.isActive == true || activeEntitlements.isNotEmpty()
        Log.d("SubscriptionVM", "isPro status set to: $isPro")
    }

    override fun onCleared() {
        super.onCleared()
        // Note: Since this is now a 'single' in Koin, onCleared might not be called 
        // as you'd expect for a standard ViewModel, but it's good practice.
        // Purchases.sharedInstance.updatedCustomerInfoListener = null
    }
}
