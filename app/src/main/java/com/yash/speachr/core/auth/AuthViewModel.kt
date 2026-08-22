package com.yash.speachr.core.auth

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.yash.speachr.TAG
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
    private val repository: AuthRepository,
    private val supabase: SupabaseClient
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthState>(
        AuthState.Loading
    )

    val authState: StateFlow<AuthState> =
        _authState.asStateFlow()

    init {
        observeAuth()
    }

    private fun observeAuth() {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                _authState.value = when (status) {
                    is io.github.jan.supabase.auth.status.SessionStatus.Authenticated -> {
                        if (repository.isOnboardingCompleted()) {
                            AuthState.Authenticated
                        } else {
                            AuthState.Onboarding
                        }
                    }
                    is io.github.jan.supabase.auth.status.SessionStatus.NotAuthenticated -> {
                        AuthState.Unauthenticated
                    }
                    else -> {
                        AuthState.Loading
                    }
                }
            }
        }
    }

    fun completeOnboarding() {
        repository.setOnboardingCompleted(true)
        // If we were in Onboarding state, move to Authenticated
        if (_authState.value == AuthState.Onboarding) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun getOnboardingStep(): Int = repository.getOnboardingStep()

    fun updateOnboardingStep(step: Int) {
        repository.setOnboardingStep(step)
    }

    suspend fun signIn(request: GetCredentialRequest, context: Context, nonce: String? = null): Exception? {
        val credentialManager = CredentialManager.create(context)
        val failureMessage = "Sign in failed!"
        
        delay(250)
        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            Log.i(TAG, result.toString())

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.i(TAG, "Signed in as: ${googleIdTokenCredential.id}")
                
                // Sign in with Supabase
                repository.signInWithGoogleIdToken(googleIdTokenCredential.idToken, nonce)
            }

            Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "(☞ﾟヮﾟ)☞  Sign in Successful!  ☜(ﾟヮﾟ☜)")
            null
        } catch (e: GoogleIdTokenParsingException) {
//            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Issue with parsing received GoogleIdToken", e)
            e
        } catch (e: NoCredentialException) {
//            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": No credentials found", e)
            e
        } catch (e: GetCredentialCancellationException) {
            Toast.makeText(context, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Sign-in was cancelled", e)
            e
        } catch (e: GetCredentialCustomException) {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Issue with custom credential request", e)
            e
        } catch (e: GetCredentialException) {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Failure getting credentials", e)
            e
        } catch (e: Exception) {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Unexpected error", e)
            e
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }
}
