package ps.shanty.intellij.snt.formatter

import com.intellij.configurationStore.Property
import com.intellij.lang.properties.PropertiesLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.InvalidDataException
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import org.jdom.Element
import ps.shanty.intellij.snt.SNTLanguage

class SNTCodeStyleSettings(container: CodeStyleSettings?) : CustomCodeStyleSettings(SNTLanguage.INSTANCE.id, container) {
    @JvmField
    var SPACES_AROUND_KEY_VALUE_DELIMITER = false
    @JvmField
    var KEEP_BLANK_LINES = false

    @Property(externalName = "key_value_delimiter")
    var KEY_VALUE_DELIMITER_CODE = 0
    val delimiter: Char
        get() = DELIMITERS[KEY_VALUE_DELIMITER_CODE]

    @Throws(InvalidDataException::class)
    override fun readExternal(parentElement: Element) {
        var parentElement: Element? = parentElement
        super.readExternal(parentElement)
        parentElement = parentElement!!.getChild(tagName)
        if (parentElement != null) {
            var delimiter: Char? = null
            for (e in parentElement.getChildren("option")) {
                val fieldName = e.getAttributeValue("name")
                if ("KEY_VALUE_DELIMITER" == fieldName) {
                    val value = e.getAttributeValue("value")
                    delimiter = value[0]
                    break
                }
            }
            if (delimiter != null) {
                when (delimiter) {
                    '=' -> KEY_VALUE_DELIMITER_CODE = 0
                    ':' -> KEY_VALUE_DELIMITER_CODE = 1
                    ' ' -> KEY_VALUE_DELIMITER_CODE = 2
                }
            }
        }
    }

    companion object {
        val DELIMITERS = charArrayOf('=', ':', ' ')
        fun getInstance(project: Project?): SNTCodeStyleSettings {
            return CodeStyleSettingsManager.getSettings(project)
                .getCustomSettings(SNTCodeStyleSettings::class.java)
        }
    }
}