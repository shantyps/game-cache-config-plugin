package ps.shanty.intellij.snt.formatter

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock

/**
 * @author Dmitry Batkovich
 */
class SNTBlock constructor(
    node: ASTNode,
    alignment: Alignment?
) :
    AbstractBlock(node, null, alignment) {
    override fun buildChildren(): List<Block> {
        return emptyList()
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return null
    }

    override fun isLeaf(): Boolean {
        return true
    }

    override fun getIndent(): Indent? {
        return Indent.getAbsoluteNoneIndent()
    }
}