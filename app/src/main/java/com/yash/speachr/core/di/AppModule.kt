package com.yash.speachr.core.di

import android.util.Log
import com.yash.speachr.BuildConfig
import com.yash.speachr.core.auth.AuthRepository
import com.yash.speachr.core.auth.AuthViewModel
import com.yash.speachr.core.floating.FloatingViewModel
import com.yash.speachr.core.permissions.PermissionViewModel
import com.yash.speachr.core.repository.AudioRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Supabase
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Auth)
        }
    }

    // Ktor HttpClient
    single<HttpClient> {
        val supabase = get<SupabaseClient>()
        HttpClient(Android) {
            defaultRequest {
                url("http://localhost:3000/")
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("HTTP", message)
                    }
                }
                level = LogLevel.ALL
            }
            install(io.ktor.client.plugins.auth.Auth) {
                bearer {
                    loadTokens {
                        supabase.auth.currentAccessTokenOrNull()?.let { token ->
                            BearerTokens(token, "")
                        }
                    }
                    refreshTokens {
                        // Supabase handles refresh internally, but we can try to get new token
                        supabase.auth.currentAccessTokenOrNull()?.let { token ->
                            BearerTokens(token, "")
                        }
                    }
                }
            }
        }
    }

    // Repositories
    single { AuthRepository(get(), get()) }
    single { AudioRepository(get()) }

    // ViewModels
    viewModel { AuthViewModel(androidApplication(), get(), get()) }
    viewModel { PermissionViewModel(androidApplication()) }
    viewModel { FloatingViewModel(androidApplication(), get()) }
}
