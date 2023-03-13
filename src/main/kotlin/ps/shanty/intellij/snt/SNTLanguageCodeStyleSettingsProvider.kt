package ps.shanty.intellij.snt

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.codeStyle.properties.CodeStyleFieldAccessor
import com.intellij.application.options.codeStyle.properties.MagicIntegerConstAccessor
import com.intellij.lang.Language
import com.intellij.lang.properties.PropertiesBundle
import com.intellij.lang.properties.psi.codeStyle.PropertiesCodeStyleSettingsPanel
import com.intellij.psi.codeStyle.*
import ps.shanty.intellij.snt.formatter.SNTCodeStyleSettings
import java.lang.reflect.Field

internal class SNTLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun createConfigurable(
        baseSettings: CodeStyleSettings,
        modelSettings: CodeStyleSettings
    ): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(
            baseSettings, modelSettings,
            SNTBundle.message("properties.files.code.style.node.title")
        ) {
            override fun getHelpTopic(): String? {
                return "reference.settingsdialog.codestyle.properties"
            }

            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                return PropertiesCodeStyleSettingsPanel(settings)
            }
        }
    }

    override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings? {
        return SNTCodeStyleSettings(settings)
    }

    override fun getLanguage(): Language {
        return SNTLanguage.INSTANCE
    }

    override fun customizeSettings(
        consumer: CodeStyleSettingsCustomizable,
        settingsType: SettingsType
    ) {
        consumer.showStandardOptions("ALIGN_GROUP_FIELD_DECLARATIONS")
        consumer.showCustomOption(
            SNTCodeStyleSettings::class.java, "SPACES_AROUND_KEY_VALUE_DELIMITER",
            SNTBundle.message("insert.space.around.key.value.delimiter.code.style.settings.name"), null
        )
        consumer.showCustomOption(
            SNTCodeStyleSettings::class.java,
            "KEY_VALUE_DELIMITER_CODE",
            SNTBundle.message("key.value.delimiter.code.style.settings.name"),
            null,
            arrayOf("=", ":", SNTBundle.message("whitespace.symbol.delimeter.combobox.presentation")),
            intArrayOf(0, 1, 2)
        )
        consumer.showCustomOption(
            SNTCodeStyleSettings::class.java, "KEEP_BLANK_LINES",
            SNTBundle.message("keep.blank.lines.code.style.setting.name"), null
        )
    }

    override fun getCodeSample(settingsType: SettingsType): String? {
        return """
            key1=value
            some_key=some_value
            
            #commentaries
            last.key=some text here
            """.trimIndent()
    }

    override fun getAccessor(
        codeStyleObject: Any,
        field: Field
    ): CodeStyleFieldAccessor<*, *>? {
        if (codeStyleObject is SNTCodeStyleSettings) {
            if ("KEY_VALUE_DELIMITER_CODE" == field.name) {
                return MagicIntegerConstAccessor(
                    codeStyleObject, field, intArrayOf(0, 1, 2), arrayOf("equals", "colon", "space")
                )
            }
        }
        return super.getAccessor(codeStyleObject, field)
    }
}