package ps.shanty.intellij.formatter

import com.intellij.formatting.Block
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.formatter.DocumentBasedFormattingModel
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.yaml.psi.impl.YAMLBlockScalarImpl
import ps.shanty.intellij.parser.GameCacheConfigElementTypes

class FormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val file = formattingContext.containingFile
        val settings = formattingContext.codeStyleSettings
        val rootBlock = createBlock(FormattingContext(settings, file), formattingContext.node)
        return DocumentBasedFormattingModel(rootBlock, settings, file)
    }

    override fun getRangeAffectingIndent(file: PsiFile, offset: Int, elementAtOffset: ASTNode): TextRange? {
        return null
    }

    companion object {
        fun createBlock(
            context: ps.shanty.intellij.formatter.FormattingContext,
            node: ASTNode
        ): Block {
            val nodeType = PsiUtilCore.getElementType(node)
            if (GameCacheConfigElementTypes.BLOCK_SCALAR_ITEMS.contains(nodeType)) {
                val blockScalarNode = node.treeParent
                assert(blockScalarNode.psi is YAMLBlockScalarImpl)
                val blockScalarImpl = blockScalarNode.psi as YAMLBlockScalarImpl
                if (blockScalarImpl.getNthContentTypeChild(0) !== node) {
                    // node is not block scalar header
                    return BlockScalarItemBlock.createBlockScalarItem(context, node)
                }
            }
            assert(nodeType !== GameCacheConfigElementTypes.SEQUENCE) { "Sequence should be inlined!" }
            assert(nodeType !== GameCacheConfigElementTypes.MAPPING) { "Mapping should be inlined!" }
            assert(nodeType !== GameCacheConfigElementTypes.DOCUMENT) { "Document should be inlined!" }
            return FormattingBlock(context, node)
        }
    }
}