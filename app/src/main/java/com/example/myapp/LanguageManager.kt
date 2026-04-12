import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "lang_prefs"
    private const val KEY_LANG = "language_code"

    fun saveLanguage(context: Context, languageCode: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, languageCode).apply()
    }

    fun getLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, getSystemLanguage()) ?: "en"
    }

    fun applyLanguage(context: Context): Context {
        val languageCode = getLanguage(context)
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    private fun getSystemLanguage(): String {
        return Locale.getDefault().language
    }
}