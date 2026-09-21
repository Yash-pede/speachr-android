package com.yash.speachr.core.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A spoken language that Whisper (whisper-large-v3) can transcribe / translate into.
 *
 * @param name       English display name. This is the value persisted in user settings and the
 *                   value sent to the backend as `target_language`.
 * @param nativeName Endonym, shown in parentheses next to [name] in the picker (e.g. "हिन्दी").
 * @param code       ISO-639-1 code. Not currently sent over the wire, but kept so the backend
 *                   contract can be switched to codes without touching the UI layer.
 */
data class SpokenLanguage(
    val name: String,
    val nativeName: String,
    val code: String,
)

/** Language selected for brand-new installs. */
const val DEFAULT_LANGUAGE_NAME = "English"

internal const val USER_SETTINGS_PREFS = "user_settings"
internal const val LANGUAGE_PREF_KEY = "language"

/**
 * Every language supported by OpenAI's Whisper models, sorted alphabetically by English name.
 * "English" is intentionally just one entry among many and is only special-cased via
 * [DEFAULT_LANGUAGE_NAME].
 */
val WHISPER_LANGUAGES: List<SpokenLanguage> = listOf(
    SpokenLanguage("Afrikaans", "Afrikaans", "af"),
    SpokenLanguage("Albanian", "Shqip", "sq"),
    SpokenLanguage("Amharic", "አማርኛ", "am"),
    SpokenLanguage("Arabic", "العربية", "ar"),
    SpokenLanguage("Armenian", "Հայերեն", "hy"),
    SpokenLanguage("Assamese", "অসমীয়া", "as"),
    SpokenLanguage("Azerbaijani", "Azərbaycan", "az"),
    SpokenLanguage("Bashkir", "Башҡортса", "ba"),
    SpokenLanguage("Basque", "Euskara", "eu"),
    SpokenLanguage("Belarusian", "Беларуская", "be"),
    SpokenLanguage("Bengali", "বাংলা", "bn"),
    SpokenLanguage("Bosnian", "Bosanski", "bs"),
    SpokenLanguage("Breton", "Brezhoneg", "br"),
    SpokenLanguage("Bulgarian", "Български", "bg"),
    SpokenLanguage("Cantonese", "粵語", "yue"),
    SpokenLanguage("Catalan", "Català", "ca"),
    SpokenLanguage("Chinese (Simplified)", "简体中文", "zh"),
    SpokenLanguage("Chinese (Traditional)", "繁體中文", "zh"),
    SpokenLanguage("Croatian", "Hrvatski", "hr"),
    SpokenLanguage("Czech", "Čeština", "cs"),
    SpokenLanguage("Danish", "Dansk", "da"),
    SpokenLanguage("Dutch", "Nederlands", "nl"),
    SpokenLanguage("English", "English", "en"),
    SpokenLanguage("Estonian", "Eesti", "et"),
    SpokenLanguage("Faroese", "Føroyskt", "fo"),
    SpokenLanguage("Finnish", "Suomi", "fi"),
    SpokenLanguage("French", "Français", "fr"),
    SpokenLanguage("Galician", "Galego", "gl"),
    SpokenLanguage("Georgian", "ქართული", "ka"),
    SpokenLanguage("German", "Deutsch", "de"),
    SpokenLanguage("Greek", "Ελληνικά", "el"),
    SpokenLanguage("Gujarati", "ગુજરાતી", "gu"),
    SpokenLanguage("Haitian Creole", "Kreyòl Ayisyen", "ht"),
    SpokenLanguage("Hausa", "Hausa", "ha"),
    SpokenLanguage("Hawaiian", "ʻŌlelo Hawaiʻi", "haw"),
    SpokenLanguage("Hebrew", "עברית", "he"),
    SpokenLanguage("Hindi", "हिन्दी", "hi"),
    SpokenLanguage("Hungarian", "Magyar", "hu"),
    SpokenLanguage("Icelandic", "Íslenska", "is"),
    SpokenLanguage("Indonesian", "Bahasa Indonesia", "id"),
    SpokenLanguage("Italian", "Italiano", "it"),
    SpokenLanguage("Japanese", "日本語", "ja"),
    SpokenLanguage("Javanese", "Basa Jawa", "jw"),
    SpokenLanguage("Kannada", "ಕನ್ನಡ", "kn"),
    SpokenLanguage("Kazakh", "Қазақша", "kk"),
    SpokenLanguage("Khmer", "ខ្មែរ", "km"),
    SpokenLanguage("Korean", "한국어", "ko"),
    SpokenLanguage("Lao", "ລາວ", "lo"),
    SpokenLanguage("Latin", "Latina", "la"),
    SpokenLanguage("Latvian", "Latviešu", "lv"),
    SpokenLanguage("Lingala", "Lingála", "ln"),
    SpokenLanguage("Lithuanian", "Lietuvių", "lt"),
    SpokenLanguage("Luxembourgish", "Lëtzebuergesch", "lb"),
    SpokenLanguage("Macedonian", "Македонски", "mk"),
    SpokenLanguage("Malagasy", "Malagasy", "mg"),
    SpokenLanguage("Malay", "Bahasa Melayu", "ms"),
    SpokenLanguage("Malayalam", "മലയാളം", "ml"),
    SpokenLanguage("Maltese", "Malti", "mt"),
    SpokenLanguage("Maori", "Te Reo Māori", "mi"),
    SpokenLanguage("Marathi", "मराठी", "mr"),
    SpokenLanguage("Mongolian", "Монгол", "mn"),
    SpokenLanguage("Myanmar (Burmese)", "မြန်မာ", "my"),
    SpokenLanguage("Nepali", "नेपाली", "ne"),
    SpokenLanguage("Norwegian", "Norsk", "no"),
    SpokenLanguage("Nynorsk", "Nynorsk", "nn"),
    SpokenLanguage("Occitan", "Occitan", "oc"),
    SpokenLanguage("Pashto", "پښتو", "ps"),
    SpokenLanguage("Persian", "فارسی", "fa"),
    SpokenLanguage("Polish", "Polski", "pl"),
    SpokenLanguage("Portuguese", "Português", "pt"),
    SpokenLanguage("Punjabi", "ਪੰਜਾਬੀ", "pa"),
    SpokenLanguage("Romanian", "Română", "ro"),
    SpokenLanguage("Russian", "Русский", "ru"),
    SpokenLanguage("Sanskrit", "संस्कृतम्", "sa"),
    SpokenLanguage("Serbian", "Српски", "sr"),
    SpokenLanguage("Shona", "ChiShona", "sn"),
    SpokenLanguage("Sindhi", "سنڌي", "sd"),
    SpokenLanguage("Sinhala", "සිංහල", "si"),
    SpokenLanguage("Slovak", "Slovenčina", "sk"),
    SpokenLanguage("Slovenian", "Slovenščina", "sl"),
    SpokenLanguage("Somali", "Soomaali", "so"),
    SpokenLanguage("Spanish", "Español", "es"),
    SpokenLanguage("Sundanese", "Basa Sunda", "su"),
    SpokenLanguage("Swahili", "Kiswahili", "sw"),
    SpokenLanguage("Swedish", "Svenska", "sv"),
    SpokenLanguage("Tagalog", "Tagalog", "tl"),
    SpokenLanguage("Tajik", "Тоҷикӣ", "tg"),
    SpokenLanguage("Tamil", "தமிழ்", "ta"),
    SpokenLanguage("Tatar", "Татарча", "tt"),
    SpokenLanguage("Telugu", "తెలుగు", "te"),
    SpokenLanguage("Thai", "ไทย", "th"),
    SpokenLanguage("Tibetan", "བོད་སྐད་", "bo"),
    SpokenLanguage("Turkish", "Türkçe", "tr"),
    SpokenLanguage("Turkmen", "Türkmençe", "tk"),
    SpokenLanguage("Ukrainian", "Українська", "uk"),
    SpokenLanguage("Urdu", "اردو", "ur"),
    SpokenLanguage("Uzbek", "Oʻzbek", "uz"),
    SpokenLanguage("Vietnamese", "Tiếng Việt", "vi"),
    SpokenLanguage("Welsh", "Cymraeg", "cy"),
    SpokenLanguage("Yiddish", "ייִדיש", "yi"),
    SpokenLanguage("Yoruba", "Yorùbá", "yo"),
)

/** Looks up a language by its English [SpokenLanguage.name]; null when the value is unknown. */
fun findLanguageByName(name: String): SpokenLanguage? =
    WHISPER_LANGUAGES.firstOrNull { it.name.equals(name, ignoreCase = true) }

/**
 * Single source of truth for the user's chosen output language.
 *
 * Backed by [SharedPreferences] so the floating bubble service (which runs outside the Compose
 * tree) keeps reading the same value, while exposing a [StateFlow] so every screen — settings,
 * onboarding and the picker itself — stays in sync automatically.
 */
object TargetLanguageStore {

    private var prefs: SharedPreferences? = null

    private val _language = MutableStateFlow(DEFAULT_LANGUAGE_NAME)

    /** Currently selected language name; always in sync with persisted storage. */
    val language: StateFlow<String> = _language.asStateFlow()

    private fun prefs(context: Context): SharedPreferences =
        prefs ?: context.applicationContext
            .getSharedPreferences(USER_SETTINGS_PREFS, Context.MODE_PRIVATE)
            .also { prefs = it }

    /** Seeds the in-memory value from disk. Called once from Application.onCreate(). */
    fun init(context: Context) {
        _language.value = prefs(context)
            .getString(LANGUAGE_PREF_KEY, DEFAULT_LANGUAGE_NAME)
            ?: DEFAULT_LANGUAGE_NAME
    }

    /** Persists [name] and notifies every collector. */
    fun set(context: Context, name: String) {
        if (_language.value == name) return
        _language.value = name
        prefs(context).edit { putString(LANGUAGE_PREF_KEY, name) }
    }
}
