package ps.shanty.intellij.mod.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLCompoundValue
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLValue
import ps.shanty.intellij.mod.Mod2ElementTypes
import ps.shanty.intellij.mod.ModElementTypes
import ps.shanty.intellij.mod.psi.Mod2List
import ps.shanty.intellij.mod.psi.Mod2ListStub

class ModBlockMappingImpl : YAMLMapping, YAMLCompoundValue, YAMLValue, StubBasedPsiElementBase<Mod2ListStub>, Mod2List {

    constructor(node: ASTNode) : super(node)

    constructor(stub: Mod2ListStub) : super(stub, Mod2ElementTypes.MAPPING)

    fun getFirstKeyValue(): YAMLKeyValue {
        return findChildByType(Mod2ElementTypes.KEY_VALUE_PAIR) ?: findChildByType(ModElementTypes.KEY_VALUE_PAIR) ?: throw IllegalStateException(EMPTY_MAP_MESSAGE)
    }

    override fun getTag(): PsiElement? {
        val firstChild = firstChild
        return if (firstChild.node.elementType === YAMLTokenTypes.TAG) {
            firstChild
        } else {
            null
        }
    }

    override fun getTextValue(): String {
        return "<mapping:" + Integer.toHexString(text.hashCode()) + ">"
    }

    override fun getKeyValues(): Collection<YAMLKeyValue> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, YAMLKeyValue::class.java)
    }

    override fun getKeyValueByKey(keyText: String): YAMLKeyValue? {
        for (keyValue in keyValues) {
            if (keyText == keyValue.keyText) {
                return keyValue
            }
        }
        return null
    }

    override fun putKeyValue(keyValueToAdd: YAMLKeyValue) {
        val existingKey = getKeyValueByKey(keyValueToAdd.keyText)
        if (existingKey == null) {
            addNewKey(keyValueToAdd)
        } else {
            existingKey.replace(keyValueToAdd)
        }
    }

    private fun addNewKey(key: YAMLKeyValue) {
        val indent = YAMLUtil.getIndentToThisElement(this)
        val node = getNode()
        var place = node.lastChildNode
        var whereInsert: ASTNode? = null
        while (place != null) {
            if (place.elementType === YAMLTokenTypes.INDENT && place.textLength == indent) {
                whereInsert = place
            } else if (place.elementType === YAMLTokenTypes.EOL) {
                val next = place.treeNext
                if (next == null || next.elementType === YAMLTokenTypes.EOL) {
                    whereInsert = place
                }
            } else {
                break
            }
            place = place.treePrev
        }
        val generator = YAMLElementGenerator.getInstance(getProject())
        if (whereInsert == null) {
            add(generator.createEol())
            add(generator.createIndent(indent))
            add(key)
            return
        }
        var anchor = whereInsert.psi
        if (indent == 0 || whereInsert.elementType === YAMLTokenTypes.INDENT && lastChild.textLength == indent) {
            addAfter(key, anchor)
            return
        }
        if (whereInsert.elementType !== YAMLTokenTypes.EOL) {
            anchor = addAfter(generator.createEol(), anchor)
        }
        addAfter(generator.createIndent(indent), anchor)
        addAfter(key, anchor)
    }

    override fun deleteKeyValue(keyValueToDelete: YAMLKeyValue) {
        require(keyValueToDelete.parent === this) { "KeyValue should be the child of this" }
        YAMLUtil.deleteSurroundingWhitespace(keyValueToDelete)
        keyValueToDelete.delete()
    }

    override fun toString(): String {
        return "MOD Mapping"
    }

    companion object {
        private const val EMPTY_MAP_MESSAGE = "YAML map without any key-value"
    }
}