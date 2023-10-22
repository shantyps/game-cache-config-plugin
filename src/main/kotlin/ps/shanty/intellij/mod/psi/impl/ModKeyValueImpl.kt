package ps.shanty.intellij.mod.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.impl.YAMLKeyValueImpl
import ps.shanty.intellij.mod.ModElementTypes

class ModKeyValueImpl(node: ASTNode) : YAMLKeyValueImpl(node) {
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

    override fun toString(): String {
        return "MOD key value"
    }
}