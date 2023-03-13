package ps.shanty.intellij.snt

import com.intellij.lang.properties.psi.PropertyKeyValueFormat
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.util.Iconable
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiInvalidElementAccessException
import com.intellij.util.IncorrectOperationException
import ps.shanty.intellij.snt.psi.SNTFile

interface ISNTEntry : Navigatable, Iconable {
    fun getName(): String?
    fun setName(name: String): PsiElement
    val key: String?

    fun getValue(): String

    /**
     * Returns the value with \n, \r, \t, \f and Unicode escape characters converted to their
     * character equivalents.
     *
     * @return unescaped value, or null if no value is specified for this property.
     */
    val unescapedValue: String?

    /**
     * Returns the key with \n, \r, \t, \f and Unicode escape characters converted to their
     * character equivalents.
     *
     * @return unescaped key, or null if no key is specified for this property.
     */
    val unescapedKey: String?

    @Throws(IncorrectOperationException::class)
    fun setValue(value: String)

    @Throws(IncorrectOperationException::class)
    fun setValue(value: String, format: PropertyKeyValueFormat) {
        setValue(value)
    }

    @get:Throws(PsiInvalidElementAccessException::class)
    val sntFile: SNTFile

    /**
     * @return text of comment preceding this property. Comment-start characters ('#' and '!') are stripped from the text.
     */
    val docCommentText: String?

    /**
     * @return underlying psi element of property
     */
    val psiElement: PsiElement

    companion object {
        val EMPTY_ARRAY = arrayOfNulls<ISNTEntry>(0)
        val ARRAY_KEY: DataKey<Array<ISNTEntry>> = DataKey.create("ISNT.array")
    }
}