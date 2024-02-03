package ps.shanty.intellij.mod.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.IncorrectOperationException
import com.intellij.util.ObjectUtils
import org.jetbrains.annotations.NonNls
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.*
import ps.shanty.intellij.PluginIcons
import ps.shanty.intellij.mod.ModElementTypes
import ps.shanty.intellij.mod.psi.*
import javax.swing.Icon

class ModKeyValueImpl : YAMLKeyValue, StubBasedPsiElementBase<Mod2EntryStub>, Mod2Entry {

    constructor(node: ASTNode) : super(node)

    constructor(stub: Mod2EntryStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getKey(): PsiElement? {
        val colon = findChildByType<PsiElement>(YAMLTokenTypes.COLON) ?: return null
        var node = colon.node
        do {
            node = node!!.treePrev
        } while (ModElementTypes.BLANK_ELEMENTS.contains(PsiUtilCore.getElementType(node)))

        return if (node == null || PsiUtilCore.getElementType(node) === YAMLTokenTypes.QUESTION) {
            null
        } else {
            node.psi
        }
    }

    override fun getKeyText(): String {
        val keyElement = key ?: return ""

        /*if (keyElement is YAMLScalar) {
            return keyElement.textValue
        }
        if (keyElement is YAMLCompoundValue) {
            return keyElement.textValue
        }*/ // commented out because strangely textValue is blank?

        val text = keyElement.text
        return StringUtil.unquoteString(text)
    }

    override fun getElementIcon(flags: Int): Icon {
        return PluginIcons.LOGO
    }

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                return keyText
            }

            override fun getLocationString(): String {
                return containingFile.name
            }

            override fun getIcon(open: Boolean): Icon? {
                return null
            }
        }
    }

    @Throws(IncorrectOperationException::class)
    override fun setName(@NonNls newName: String): PsiElement {
        return YAMLUtil.rename(this, newName)
    }

    override fun getValue(): YAMLValue? {
        var child = lastChild
        while (child != null) {
            if (PsiUtilCore.getElementType(child) === YAMLTokenTypes.COLON) {
                return null
            }
            if (child is YAMLValue) {
                return child
            }
            child = child.prevSibling
        }
        return null
    }

    override fun getValueText(): String {
        val value = getValue()
        if (value is YAMLScalar) {
            return value.textValue
        } else if (value is YAMLCompoundValue) {
            return value.textValue
        }
        return ""
    }

    override fun setValue(value: YAMLValue) {
        adjustWhitespaceToContentType(value is YAMLScalar)
        if (getValue() != null) {
            getValue()!!.replace(value)
            return
        }
        val generator = YAMLElementGenerator.getInstance(getProject())
        if (isExplicit()) {
            if (findChildByType<PsiElement?>(YAMLTokenTypes.COLON) == null) {
                add(generator.createColon())
                add(generator.createSpace())
                add(value)
            }
        } else {
            add(value)
        }
    }

    private fun adjustWhitespaceToContentType(isScalar: Boolean) {
        assert(getKey() != null)
        var key = getKey()
        if (key?.nextSibling != null && key.nextSibling?.node?.elementType === YAMLTokenTypes.COLON) {
            key = key.nextSibling
        }
        while (key?.nextSibling != null && key.nextSibling !is YAMLValue) {
            key.nextSibling.delete()
        }
        val generator = YAMLElementGenerator.getInstance(getProject())
        if (isScalar) {
            addAfter(generator.createSpace(), key)
        } else {
            val indent = YAMLUtil.getIndentToThisElement(this)
            addAfter(generator.createIndent(indent + 2), key)
            addAfter(generator.createEol(), key)
        }
    }

    private fun isExplicit(): Boolean {
        val child = getNode().firstChildNode
        return child != null && child.elementType === YAMLTokenTypes.QUESTION
    }

    override fun getParentMapping(): YAMLMapping? {
        return ObjectUtils.tryCast(super.getParent(), YAMLMapping::class.java)
    }

    override fun toString(): String {
        return "MOD key value"
    }
}