package ps.shanty.intellij.mod

import com.intellij.application.options.*
import com.intellij.application.options.codeStyle.properties.CodeStyleFieldAccessor
import com.intellij.application.options.codeStyle.properties.MagicIntegerConstAccessor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.*
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions
import ps.shanty.intellij.mod.formatter.ModCodeStyleSettings
import java.lang.reflect.Field
import javax.swing.JCheckBox

class ModLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    private object Holder {
        val ALIGN_VALUES = intArrayOf(
            ModCodeStyleSettings.DO_NOT_ALIGN,
            ModCodeStyleSettings.ALIGN_ON_COLON,
            ModCodeStyleSettings.ALIGN_ON_VALUE
        )

        val ALIGN_OPTIONS = arrayOf(
            ModBundle.message("ModLanguageCodeStyleSettingsProvider.align.options.no"),
            ModBundle.message("ModLanguageCodeStyleSettingsProvider.align.options.colon"),
            ModBundle.message("ModLanguageCodeStyleSettingsProvider.align.options.value")
        )
    }

    override fun createConfigurable(
        settings: CodeStyleSettings,
        originalSettings: CodeStyleSettings
    ): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(settings, originalSettings, ModLanguage.INSTANCE.displayName) {
            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                val currentSettings = currentSettings
                return object : TabbedLanguageCodeStylePanel(ModLanguage.INSTANCE, currentSettings, settings) {
                    override fun initTabs(settings: CodeStyleSettings) {
                        addIndentOptionsTab(settings)
                        addSpacesTab(settings)
                        addWrappingAndBracesTab(settings)
                    }
                }
            }

            override fun getHelpTopic(): String {
                return "reference.settingsdialog.codestyle.mod"
            }
        }
    }

    override fun getConfigurableDisplayName(): String {
        return ModLanguage.INSTANCE.displayName
    }

    override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings {
        return ModCodeStyleSettings(settings)
    }

    override fun customizeDefaults(
        commonSettings: CommonCodeStyleSettings,
        indentOptions: IndentOptions
    ) {
        indentOptions.INDENT_SIZE = 2
        indentOptions.CONTINUATION_INDENT_SIZE = 2
        indentOptions.USE_TAB_CHARACTER = false
        commonSettings.SPACE_WITHIN_BRACES = true
        commonSettings.SPACE_WITHIN_BRACKETS = true
    }

    override fun getIndentOptionsEditor(): IndentOptionsEditor {
        return YAMLIndentOptionsEditor(this)
    }

    override fun getLanguage(): Language {
        return ModLanguage.INSTANCE
    }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        if (settingsType == SettingsType.INDENT_SETTINGS) {
            consumer.showStandardOptions("INDENT_SIZE", "KEEP_INDENTS_ON_EMPTY_LINES")
        } else if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions("SPACE_WITHIN_BRACES", "SPACE_WITHIN_BRACKETS")
            consumer.showCustomOption(
                ModCodeStyleSettings::class.java, "SPACE_BEFORE_COLON", ModBundle.message(
                    "ModLanguageCodeStyleSettingsProvider.label.before"
                ), CodeStyleSettingsCustomizableOptions.getInstance().SPACES_OTHER
            )
        } else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            consumer.showStandardOptions("KEEP_LINE_BREAKS")
            consumer.showCustomOption(
                ModCodeStyleSettings::class.java,
                "ALIGN_VALUES_PROPERTIES",
                ModBundle.message("ModLanguageCodeStyleSettingsProvider.align.values"),
                null,
                Holder.ALIGN_OPTIONS,
                Holder.ALIGN_VALUES
            )
            consumer.showCustomOption(
                ModCodeStyleSettings::class.java,
                "SEQUENCE_ON_NEW_LINE",
                ModBundle.message("ModLanguageCodeStyleSettingsProvider.sequence.on.new.line"),
                ModBundle.message("ModLanguageCodeStyleSettingsProvider.group.sequence.value")
            )
            consumer.showCustomOption(
                ModCodeStyleSettings::class.java,
                "BLOCK_MAPPING_ON_NEW_LINE",
                ModBundle.message("ModLanguageCodeStyleSettingsProvider.block.mapping.on.new.line"),
                ModBundle.message("ModLanguageCodeStyleSettingsProvider.group.sequence.value")
            )
            consumer.showCustomOption(
                ModCodeStyleSettings::class.java,
                "AUTOINSERT_SEQUENCE_MARKER",
                ModBundle.message("ModLanguageCodeStyleSettingsProvider.autoinsert.sequence.marker"),
                ModBundle.message("ModLanguageCodeStyleSettingsProvider.group.sequence.value")
            )
        }
    }

    override fun getCodeSample(settingsType: SettingsType): String? {
        return CodeStyleAbstractPanel.readFromFile(ModBundle::class.java, "indents.yml")
    }

    private class YAMLIndentOptionsEditor(provider: LanguageCodeStyleSettingsProvider?) :
        SmartIndentOptionsEditor(provider) {
        private var myIndentSequence: JCheckBox? = null
        override fun addComponents() {
            super.addComponents()
            myIndentSequence =
                JCheckBox(ModBundle.message("ModLanguageCodeStyleSettingsProvider.indent.sequence.value"))
            add(myIndentSequence)
        }

        override fun setEnabled(enabled: Boolean) {
            super.setEnabled(enabled)
            myIndentSequence!!.isEnabled = enabled
        }

        override fun isModified(settings: CodeStyleSettings, options: IndentOptions): Boolean {
            var isModified = super.isModified(settings, options)
            val yamlSettings = settings.getCustomSettings(
                ModCodeStyleSettings::class.java
            )
            isModified = isModified or isFieldModified(myIndentSequence, yamlSettings.INDENT_SEQUENCE_VALUE)
            return isModified
        }

        override fun apply(settings: CodeStyleSettings, options: IndentOptions) {
            super.apply(settings, options)
            val yamlSettings = settings.getCustomSettings(
                ModCodeStyleSettings::class.java
            )
            yamlSettings.INDENT_SEQUENCE_VALUE = myIndentSequence!!.isSelected
        }

        override fun reset(settings: CodeStyleSettings, options: IndentOptions) {
            super.reset(settings, options)
            val yamlSettings = settings.getCustomSettings(
                ModCodeStyleSettings::class.java
            )
            myIndentSequence!!.isSelected = yamlSettings.INDENT_SEQUENCE_VALUE
        }
    }

    override fun getAccessor(
        codeStyleObject: Any,
        field: Field
    ): CodeStyleFieldAccessor<*, *>? {
        return if (codeStyleObject is ModCodeStyleSettings && "ALIGN_VALUES_PROPERTIES" == field.name) {
            MagicIntegerConstAccessor(
                codeStyleObject, field,
                Holder.ALIGN_VALUES, arrayOf("do_not_align", "on_colon", "on_value")
            )
        } else super.getAccessor(codeStyleObject, field)
    }
}