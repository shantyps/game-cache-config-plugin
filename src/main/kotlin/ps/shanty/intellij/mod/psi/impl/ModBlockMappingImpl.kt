package ps.shanty.intellij.mod.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.SmartList
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl
import ps.shanty.intellij.mod.ModElementGenerator
import ps.shanty.intellij.mod.ModElementTypes

class ModBlockMappingImpl(node: ASTNode) : YAMLBlockMappingImpl(node) {
    override fun getFirstKeyValue(): YAMLKeyValue {
        return findChildByType(ModElementTypes.KEY_VALUE_PAIR) ?: throw IllegalStateException(EMPTY_MAP_MESSAGE)
    }

    override fun insertKeyValueAtOffset(keyValue: YAMLKeyValue, offset: Int) {
        var offSetLocal = offset

        if (offSetLocal < textRange.startOffset) {
            offSetLocal = textRange.startOffset
        }

        if (offSetLocal > textRange.endOffset) {
            var nextLeaf = PsiTreeUtil.nextLeaf(this)
            val toBeRemoved = SmartList<PsiElement>()
            while (ModElementTypes.SPACE_ELEMENTS.contains(PsiUtilCore.getElementType(nextLeaf))) {
                if (offSetLocal >= nextLeaf!!.textRange.startOffset) {
                    toBeRemoved.add(nextLeaf)
                }
                nextLeaf = PsiTreeUtil.nextLeaf(nextLeaf)
            }
            for (leaf in toBeRemoved) {
                add(leaf)
            }
            for (leaf in toBeRemoved) {
                leaf.delete()
            }

            addNewKeyToTheEnd(keyValue)
            return
        }
        super.insertKeyValueAtOffset(keyValue, offset)
    }

    private fun addNewKeyToTheEnd(key: YAMLKeyValue) {
        val indent = YAMLUtil.getIndentToThisElement(this)
        val generator = ModElementGenerator.getInstance(project)
        val lastChildType = PsiUtilCore.getElementType(lastChild)
        if (indent == 0) {
            if (lastChildType !== YAMLTokenTypes.EOL) {
                add(generator.createEol())
            }
        } else if (!(lastChildType === YAMLTokenTypes.INDENT && lastChild.textLength == indent)) {
            add(generator.createEol())
            add(generator.createIndent(indent))
        }
        add(key)
    }
}