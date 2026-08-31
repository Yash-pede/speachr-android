package com.yash.speachr.ui.screens.onboarding.auth

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.yash.speachr.core.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

@Composable
fun BottomSheet(webClientId: String, authViewModel: AuthViewModel = koinViewModel()) {
    val context = LocalContext.current

    // LaunchedEffect is used to run a suspend function when the composable is first launched.
    LaunchedEffect(Unit) {
        // Create a Google ID option with filtering by authorized accounts enabled.
        val (rawNonce, hashedNonce) = generateNonce()
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setNonce(hashedNonce)
            .build()

        // Create a credential request with the Google ID option.
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Attempt to sign in with the created request using an authorized account
        val e = authViewModel.signIn(request, context, rawNonce)
        // If the sign-in fails with NoCredentialException, there are no authorized accounts.
        // In this case, we attempt to sign in again with filtering disabled.
        if (e is NoCredentialException) {
            val (rawNonceFalse, hashedNonceFalse) = generateNonce()
            val googleIdOptionFalse = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(hashedNonceFalse)
                .build()

            val requestFalse: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOptionFalse)
                .build()

            authViewModel.signIn(requestFalse, context, rawNonceFalse)
        }
    }
}

// Generate a raw nonce and its SHA-256 hash for Google/Supabase double-nonce pattern
fun generateNonce(): Pair<String, String> {
    val rawNonce = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        .let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }

    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(rawNonce.toByteArray())
    val hashedNonce = digest.joinToString("") { "%02x".format(it) }

    return Pair(rawNonce, hashedNonce)
}
