package com.yash.speachr.core.auth

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo

class AuthRepository(context: Context, private val supabase: SupabaseClient) {

    private val sharedPrefs = context.getSharedPreferences("speachr_prefs", Context.MODE_PRIVATE)

    suspend fun signInWithGoogleIdToken(
        googleIdToken: String,
        nonce: String? = null
    ): UserInfo? {
        supabase.auth.signInWith(IDToken) {
            idToken = googleIdToken
            provider = Google
            this.nonce = nonce
        }
        return supabase.auth.currentUserOrNull()
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    fun isOnboardingCompleted(): Boolean {
        return sharedPrefs.getBoolean("onboarding_completed", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        sharedPrefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    fun getOnboardingStep(): Int {
        return sharedPrefs.getInt("onboarding_step", 0)
    }

    fun setOnboardingStep(step: Int) {
        sharedPrefs.edit().putInt("onboarding_step", step).apply()
    }
}
