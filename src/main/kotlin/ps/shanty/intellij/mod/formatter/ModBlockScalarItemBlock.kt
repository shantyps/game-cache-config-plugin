package ps.shanty.intellij.mod.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.impl.YAMLBlockScalarImpl

/**
 * This class is a special case for block scalar items. We need to preserve additional spaces:
 * <pre>`key: |
 * First line
 * Second line
`</pre> *
 * Could be shifted to
 * <pre>`key: |
 * First line
 * Second line
`</pre> *
 *
 * And if there is explicit indent number then we need to preserve all spaces and could not shift block scalar items related to key
 *
 * See [8.1. Block Scalar Styles](http://yaml.org/spec/1.2/spec.html#id2793652)
 */
internal class ModBlockScalarItemBlock private constructor(
    val myRange: TextRange,
    val myIndent: Indent?,
    val myAlignment: Alignment?
) :
    Block {
    override fun toString(): String {
        return "GameCacheConfigBlockScalarItemBlock($textRange)"
    }

    override fun getTextRange(): TextRange {
        return myRange
    }

    override fun getSubBlocks(): List<Block> {
        return emptyList()
    }

    override fun getWrap(): Wrap? {
        return null
    }

    override fun getIndent(): Indent? {
        return myIndent
    }

    override fun getAlignment(): Alignment? {
        return myAlignment
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return null
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        return ChildAttributes(null, null)
    }

    override fun isIncomplete(): Boolean {
        return false
    }

    override fun isLeaf(): Boolean {
        return true
    }

    companion object {
        /** @return null iff it is not block scalar item
         */
        fun createBlockScalarItem(context: ps.shanty.intellij.mod.formatter.ModFormattingContext, node: ASTNode): Block {
            val blockScalarNode = node.treeParent
            val blockScalarImpl = blockScalarNode.psi as YAMLBlockScalarImpl

            // possible performance problem: parent full indent for every block scalar line
            val parentFullIndent =
                ps.shanty.intellij.mod.formatter.ModBlockScalarItemBlock.Companion.getParentFullIndent(
                    context,
                    blockScalarNode.treeParent
                )
            val indent: Indent
            val range: TextRange
            var alignment: Alignment? = null
            val oldOffset = Math.max(
                ps.shanty.intellij.mod.formatter.ModBlockScalarItemBlock.Companion.getNodeFullIndent(
                    node
                ) - parentFullIndent, 0)
            if (blockScalarImpl.hasExplicitIndent()) {
                range = TextRange(node.startOffset - oldOffset, node.textRange.endOffset)
                indent = Indent.getSpaceIndent(0, true)
            } else {
                // possible performance problem: calculating first line offset for every block scalar line
                val needOffset = Math.max(oldOffset - ps.shanty.intellij.mod.formatter.ModBlockScalarItemBlock.Companion.getFirstLineOffset(
                    context,
                    blockScalarImpl
                ), 0)
                range = TextRange(node.startOffset - needOffset, node.textRange.endOffset)
                alignment = context.computeAlignment(node)
                indent = Indent.getNormalIndent(true)
            }
            return ps.shanty.intellij.mod.formatter.ModBlockScalarItemBlock(range, indent, alignment)
        }

        private fun getFirstLineOffset(context: ps.shanty.intellij.mod.formatter.ModFormattingContext, blockScalarPsi: YAMLBlockScalarImpl): Int {
            val parentFullIndent =
                ps.shanty.intellij.mod.formatter.ModBlockScalarItemBlock.Companion.getParentFullIndent(
                    context,
                    blockScalarPsi.node.treeParent
                )
            val firstLine = blockScalarPsi.getNthContentTypeChild(1) ?: return 0
            return Math.max(
                ps.shanty.intellij.mod.formatter.ModBlockScalarItemBlock.Companion.getNodeFullIndent(
                    firstLine
                ) - parentFullIndent, 0)
        }

        private fun getParentFullIndent(context: ps.shanty.intellij.mod.formatter.ModFormattingContext, node: ASTNode): Int {
            val fullText = context.fullText
            val start = node.textRange.startOffset
            for (cur in start - 1 downTo 0) {
                if (fullText[cur] == '\n') {
                    return start - cur - 1
                }
                if (start - cur > 1000) {
                    // So big indent has no practical use...
                    return 0
                }
            }
            return start
        }

        private fun getNodeFullIndent(node: ASTNode): Int {
            val indentNode = node.treePrev
            return if (indentNode == null || indentNode.elementType !== YAMLTokenTypes.INDENT) {
                0
            } else indentNode.textLength
        }
    }
}