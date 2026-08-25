package com.yash.speachr.core.billing

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
        checkProStatus(customerInfo)
    }

    init {
        Purchases.sharedInstance.updatedCustomerInfoListener = customerInfoListener
        updateCustomerInfo()
    }

    fun updateCustomerInfo() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                checkProStatus(customerInfo)
            }

            override fun onError(error: PurchasesError) {
                // Handle error
            }
        })
    }

    private fun checkProStatus(customerInfo: CustomerInfo) {
        // Check for "pro" entitlement. Adjust "pro" to your actual entitlement ID.
        isPro = customerInfo.entitlements["pro"]?.isActive == true
    }

    override fun onCleared() {
        super.onCleared()
        Purchases.sharedInstance.updatedCustomerInfoListener = null
    }
}
