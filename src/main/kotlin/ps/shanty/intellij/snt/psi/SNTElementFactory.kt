package ps.shanty.intellij.snt.psi

import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.psi.PropertiesResourceBundleUtil
import com.intellij.lang.properties.psi.PropertyKeyValueFormat
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataCache
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFileFactory
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.NonNls
import ps.shanty.intellij.snt.ISNTEntry
import ps.shanty.intellij.snt.SNTFileType
import ps.shanty.intellij.snt.formatter.SNTCodeStyleSettings
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

object SNTElementFactory {
    private val SYSTEM_PROPERTIES_KEY = Key.create<SNTFile>("system.snt.file")
    private val PROPERTIES: UserDataCache<SNTFile, Project, Void> =
        object : UserDataCache<SNTFile, Project, Void>("system.snt.file") {
            protected override fun compute(project: Project, p: Void): SNTFile {
                return createPropertiesFile(project, System.getProperties(), "system")
            }
        }

    @JvmOverloads
    fun createProperty(
        project: Project,
        name: @NonNls String,
        value: @NonNls String,
        delimiter: Char?,
        format: PropertyKeyValueFormat = PropertyKeyValueFormat.PRESENTABLE
    ): ISNTEntry {
        val text = getPropertyText(name, value, delimiter, project, format)
        val dummyFile = createPropertiesFile(project, text)
        return dummyFile.properties[0]
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2021.3")
    @Deprecated("use {@link #createProperty(Project, String, String, Character)}")
    fun createProperty(
        project: Project,
        name: @NonNls String,
        value: @NonNls String
    ): ISNTEntry {
        return createProperty(project, name, value, null)
    }

    fun getPropertyText(
        name: @NonNls String,
        value: @NonNls String,
        delimiter: @NonNls Char?,
        project: Project?,
        format: PropertyKeyValueFormat
    ): String {
        var delimiter = delimiter
        if (delimiter == null) {
            delimiter = if (project == null) '=' else SNTCodeStyleSettings.getInstance(project).delimiter
        }
        return (if (format != PropertyKeyValueFormat.FILE) escape(name) else name) + delimiter + escapeValue(
            value,
            delimiter,
            format
        )
    }

    fun createPropertiesFile(project: Project, text: @NonNls String): SNTFile {
        val filename: @NonNls String = "dummy." + SNTFileType.SNT.defaultExtension
        return PsiFileFactory.getInstance(project)
            .createFileFromText(filename, SNTFileType.SNT, text) as SNTFile
    }

    fun createPropertiesFile(project: Project, properties: Properties, fileName: String): SNTFile {
        val stream = ByteArrayOutputStream()
        try {
            properties.store(stream, "")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        val filename: @NonNls String = fileName + "." + SNTFileType.SNT.defaultExtension
        return PsiFileFactory.getInstance(project)
            .createFileFromText(filename, SNTFileType.SNT, stream.toString()) as SNTFile
    }

    @Synchronized
    fun getSystemProperties(project: Project): SNTFile {
        var systemPropertiesFile = project.getUserData(SYSTEM_PROPERTIES_KEY)
        if (systemPropertiesFile == null) {
            project.putUserData(
                SYSTEM_PROPERTIES_KEY,
                createPropertiesFile(project, System.getProperties(), "system").also {
                    systemPropertiesFile = it
                })
        }
        return systemPropertiesFile!!
    }

    private fun escape(name: String): String {
        var name = name
        if (StringUtil.startsWithChar(name, '#') || StringUtil.startsWithChar(name, '!')) {
            name = "\\" + name
        }
        return StringUtil.escapeChars(name, '=', ':', ' ', '\t')
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2021.3")
    @Deprecated("use {@link #escapeValue(String, char, PropertyKeyValueFormat)} instead")
    fun escapeValue(value: String?, delimiter: Char): String {
        return escapeValue(value, delimiter, PropertyKeyValueFormat.PRESENTABLE)
    }

    fun escapeValue(value: String?, delimiter: Char, format: PropertyKeyValueFormat?): String {
        return PropertiesResourceBundleUtil.convertValueToFileFormat(value!!, delimiter, format!!)
    }
}