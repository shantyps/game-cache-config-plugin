package ps.shanty.intellij.formatter

import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.SmartList
import ps.shanty.intellij.parser.GameCacheConfigElementTypes

internal class FormattingBlock(private val myContext: FormattingContext, node: ASTNode) :
    AbstractBlock(node, null, myContext.computeAlignment(node)) {
    private val myIndent: Indent?
    private val myNewChildIndent: Indent?
    private val myIsIncomplete: Boolean
    private val myTextRange: TextRange

    init {
        myIndent = myContext.computeBlockIndent(myNode)
        myIsIncomplete = myContext.isIncomplete(myNode)
        myNewChildIndent = myContext.computeNewChildIndent(myNode)
        myTextRange = myNode.textRange
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return myContext.computeSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean {
        return false
    }

    override fun isIncomplete(): Boolean {
        return myIsIncomplete
    }

    override fun getIndent(): Indent? {
        return myIndent
    }

    override fun getTextRange(): TextRange {
        return myTextRange
    }

    override fun getChildIndent(): Indent? {
        return myNewChildIndent
    }

    override fun buildChildren(): List<Block> {
        return buildSubBlocks(myContext, myNode)
    }

    private fun buildSubBlocks(context: FormattingContext, node: ASTNode): List<Block> {
        val res: MutableList<Block> = SmartList()
        var subNode = node.firstChildNode
        while (subNode != null) {
            val subNodeType = PsiUtilCore.getElementType(subNode)
            if (GameCacheConfigElementTypes.SPACE_ELEMENTS.contains(subNodeType)) {
                // just skip them (comment processed above)
            } else if (GameCacheConfigElementTypes.SCALAR_QUOTED_STRING === subNodeType) {
                res.addAll(buildSubBlocks(context, subNode))
            } else if (GameCacheConfigElementTypes.CONTAINERS.contains(subNodeType)) {
                res.addAll(
                    substituteInjectedBlocks(
                        context.mySettings,
                        buildSubBlocks(context, subNode),
                        subNode, wrap, context.computeAlignment(subNode)
                    )
                )
            } else {
                res.add(FormattingModelBuilder.createBlock(context, subNode))
            }
            subNode = subNode.treeNext
        }
        return res
    }
}