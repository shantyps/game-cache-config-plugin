package ps.shanty.intellij.snt.psi

import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.ResourceBundle
import com.intellij.lang.properties.psi.PropertyKeyValueFormat
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.NonNls
import ps.shanty.intellij.snt.ISNTEntry
import java.util.*

interface SNTFile {
    fun getContainingFile(): PsiFile

    val properties: List<ISNTEntry>

    fun findPropertyByKey(key: @NonNls String): ISNTEntry

    fun findPropertiesByKey(key: @NonNls String): List<ISNTEntry>
    val locale: Locale

    @ApiStatus.ScheduledForRemoval(inVersion = "2021.2")
    @Deprecated(
        """use {@link #addProperty(String, String)} instead
    """
    )
    @Throws(
        IncorrectOperationException::class
    )
    fun addProperty(property: ISNTEntry): PsiElement

    @Throws(IncorrectOperationException::class)
    fun addPropertyAfter(property: ISNTEntry, anchor: ISNTEntry?): PsiElement

    @Throws(IncorrectOperationException::class)
    fun addPropertyAfter(key: String, value: String, anchor: ISNTEntry?): ISNTEntry
    fun addProperty(key: String, value: String): ISNTEntry {
        return addProperty(key, value, PropertyKeyValueFormat.PRESENTABLE)
    }

    fun addProperty(key: String, value: String, format: PropertyKeyValueFormat): ISNTEntry

    val namesMap: Map<String, String>
    fun getName(): String
    fun getVirtualFile(): VirtualFile?

    fun getParent(): PsiDirectory?
    fun getProject(): Project
    fun getText(): String?

    val isAlphaSorted: Boolean
}