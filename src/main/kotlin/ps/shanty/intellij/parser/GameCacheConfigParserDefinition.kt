package ps.shanty.intellij.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiParser
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.jetbrains.yaml.YAMLParserDefinition
import org.jetbrains.yaml.psi.impl.*

class GameCacheConfigParserDefinition : YAMLParserDefinition() {

    override fun createElement(node: ASTNode?): PsiElement {
        val type = node!!.elementType
        if (type === GameCacheConfigElementTypes.DOCUMENT) {
            return YAMLDocumentImpl(node)
        }
        if (type === GameCacheConfigElementTypes.KEY_VALUE_PAIR) {
            return GameCacheConfigKeyValueImpl(node)
        }
        if (type === GameCacheConfigElementTypes.COMPOUND_VALUE) {
            return YAMLCompoundValueImpl(node)
        }
        if (type === GameCacheConfigElementTypes.SEQUENCE) {
            return YAMLBlockSequenceImpl(node)
        }
        if (type === GameCacheConfigElementTypes.MAPPING) {
            return YAMLBlockMappingImpl(node)
        }
        if (type === GameCacheConfigElementTypes.SEQUENCE_ITEM) {
            return YAMLSequenceItemImpl(node)
        }
        if (type === GameCacheConfigElementTypes.HASH) {
            return YAMLHashImpl(node)
        }
        if (type === GameCacheConfigElementTypes.ARRAY) {
            return YAMLArrayImpl(node)
        }
        if (type === GameCacheConfigElementTypes.SCALAR_LIST_VALUE) {
            return YAMLScalarListImpl(node)
        }
        if (type === GameCacheConfigElementTypes.SCALAR_TEXT_VALUE) {
            return YAMLScalarTextImpl(node)
        }
        if (type === GameCacheConfigElementTypes.SCALAR_PLAIN_VALUE) {
            return YAMLPlainTextImpl(node)
        }
        if (type === GameCacheConfigElementTypes.SCALAR_QUOTED_STRING) {
            return YAMLQuotedTextImpl(node)
        }
        if (type === GameCacheConfigElementTypes.ANCHOR_NODE) {
            return YAMLAnchorImpl(node)
        }
        if (type === GameCacheConfigElementTypes.ALIAS_NODE) {
            return YAMLAliasImpl(node)
        }
        return YAMLPsiElementImpl(node)
    }

    override fun createParser(project: Project?): PsiParser {
        return GameCacheConfigParser()
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return GameCacheConfigFileImpl(viewProvider)
    }

    override fun getStringLiteralElements(): TokenSet {
        return GameCacheConfigElementTypes.TEXT_SCALAR_ITEMS
    }

    override fun getFileNodeType(): IFileElementType {
        return GameCacheConfigElementTypes.FILE
    }
}