package ps.shanty.intellij.snt.formatter

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.lang.properties.PropertiesLanguage
import com.intellij.lang.properties.parsing.PropertiesElementTypes
import com.intellij.lang.properties.parsing.PropertiesTokenTypes
import com.intellij.lang.properties.psi.codeStyle.PropertiesCodeStyleSettings
import com.intellij.lang.properties.psi.impl.PropertyKeyImpl
import com.intellij.lang.properties.psi.impl.PropertyValueImpl
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.TokenSet
import ps.shanty.intellij.snt.SNTElementTypes

/**
 * @author Dmitry Batkovich
 */
internal class SNTRootBlock(
    node: ASTNode,
    private val mySettings: CodeStyleSettings
) : AbstractBlock(node, null, Alignment.createAlignment()) {
    private val mySeparatorAlignment: Alignment

    init {
        mySeparatorAlignment = Alignment.createAlignment(true, Alignment.Anchor.LEFT)
    }

    override fun buildChildren(): List<Block> {
        val result: MutableList<Block> = ArrayList()
        var child = myNode.firstChildNode
        while (child != null) {
            if (child !is PsiWhiteSpace) {
                if (child.elementType === SNTElementTypes.PROPERTIES_LIST) {
                    var propertyNode = child.firstChildNode
                    while (propertyNode != null) {
                        if (propertyNode.elementType === SNTElementTypes.PROPERTY) {
                            collectSNTBlock(propertyNode, result)
                        } else if (PropertiesTokenTypes.END_OF_LINE_COMMENT == propertyNode.elementType || PropertiesTokenTypes.BAD_CHARACTER == propertyNode.elementType) {
                            result.add(SNTBlock(propertyNode, null))
                        }
                        propertyNode = propertyNode.treeNext
                    }
                } else if (PropertiesTokenTypes.BAD_CHARACTER == child.elementType) {
                    result.add(SNTBlock(child, null))
                }
            }
            if (PropertiesTokenTypes.END_OF_LINE_COMMENT == child.elementType) {
                result.add(SNTBlock(child, null))
            }
            child = child.treeNext
        }
        return result
    }

    override fun getChildIndent(): Indent? {
        return Indent.getNoneIndent()
    }

    private fun collectSNTBlock(propertyNode: ASTNode, collector: MutableList<in Block>) {
        val nonWhiteSpaces = propertyNode.getChildren(
            TokenSet.create(
                PropertiesTokenTypes.KEY_CHARACTERS,
                PropertiesTokenTypes.KEY_VALUE_SEPARATOR,
                PropertiesTokenTypes.VALUE_CHARACTERS
            )
        )
        val alignment =
            if (mySettings.getCommonSettings(PropertiesLanguage.INSTANCE).ALIGN_GROUP_FIELD_DECLARATIONS) mySeparatorAlignment else null
        var hasKVSeparator = false
        for (node in nonWhiteSpaces) {
            if (node is PropertyKeyImpl) {
                collector.add(SNTBlock(node, null))
            } else if (PropertiesTokenTypes.KEY_VALUE_SEPARATOR == node.elementType) {
                collector.add(SNTBlock(node, alignment))
                hasKVSeparator = true
            } else if (node is PropertyValueImpl) {
                if (hasKVSeparator) {
                    collector.add(SNTBlock(node, null))
                } else {
                    collector.add(SNTBlock(node, alignment))
                }
            }
        }
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        if (child1 == null) {
            return null
        }
        return if (mySettings.getCustomSettings(SNTCodeStyleSettings::class.java).SPACES_AROUND_KEY_VALUE_DELIMITER &&
            (isSeparator(child1) || isSeparator(child2)) || isKeyValue(child1, child2)
        ) Spacing.createSpacing(1, 1, 0, true, 0) else Spacing.createSpacing(
            0, 0, 0, true,
            if (mySettings.getCustomSettings(SNTCodeStyleSettings::class.java).KEEP_BLANK_LINES) 999 else 0
        )
    }

    override fun isLeaf(): Boolean {
        return false
    }

    companion object {
        private fun isKeyValue(maybeKey: Block, maybeValue: Block): Boolean {
            return if (maybeKey !is SNTBlock ||
                PropertiesTokenTypes.KEY_CHARACTERS != maybeKey.node.elementType
            ) {
                false
            } else maybeValue is SNTBlock && PropertiesTokenTypes.VALUE_CHARACTERS == maybeValue.node.elementType
        }

        private fun isSeparator(block: Block): Boolean {
            return block is SNTBlock && PropertiesTokenTypes.KEY_VALUE_SEPARATOR == block.node.elementType
        }
    }
}