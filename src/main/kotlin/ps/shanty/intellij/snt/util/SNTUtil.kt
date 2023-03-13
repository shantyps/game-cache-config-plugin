package ps.shanty.intellij.snt.util

import com.intellij.lang.properties.PropertiesUtil
import com.intellij.openapi.util.Pair
import ps.shanty.intellij.snt.ISNTEntry
import ps.shanty.intellij.snt.psi.SNTFile
import java.util.*
import java.util.regex.Pattern

object SNTUtil {
    private val LOCALE_PATTERN = Pattern.compile("(_[a-zA-Z]{2,8}(_[a-zA-Z]{2}|[0-9]{3})?(_[\\w\\-]+)?)\\.[^_]+$")
    private val DEFAULT_LOCALE = Locale("", "", "")
    
    fun getLocale(sntFile: SNTFile): Locale {
        return DEFAULT_LOCALE
    }

    private fun getLocale(suffix: String?): Locale {
        return getLocaleAndTrimmedSuffix(suffix).getFirst()
    }

    private fun getLocaleAndTrimmedSuffix(suffix: String?): Pair<Locale, String?> {
        val matcher = LOCALE_PATTERN.matcher(suffix)
        if (matcher.find()) {
            val rawLocale = matcher.group(1)
            val splitRawLocale = rawLocale.split("_".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if (splitRawLocale.size > 1 && splitRawLocale[1].length >= 2) {
                val language = splitRawLocale[1]
                val country = if (splitRawLocale.size > 2) splitRawLocale[2] else ""
                val variant = if (splitRawLocale.size > 3) splitRawLocale[3] else ""
                val trimmedSuffix = StringBuilder(language)
                if (country.isNotEmpty()) {
                    trimmedSuffix.append("_").append(country)
                }
                if (variant.isNotEmpty()) {
                    trimmedSuffix.append("_").append(variant)
                }
                return Pair.create(Locale(language, country, variant), trimmedSuffix.toString())
            }
        }
        return Pair.create(PropertiesUtil.DEFAULT_LOCALE, "")
    }

    fun isAlphaSorted(properties: Collection<ISNTEntry>): Boolean {
        var previousKey: String? = null
        for (property in properties) {
            val key = property.key ?: return false
            if (previousKey != null && String.CASE_INSENSITIVE_ORDER.compare(previousKey, key) > 0) {
                return false
            }
            previousKey = key
        }
        return true
    }
}