package ps.shanty.intellij.mod

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
import ps.shanty.intellij.mod.parser.ModParser
import ps.shanty.intellij.mod.psi.impl.*

class ModParserDefinition : YAMLParserDefinition() {

    override fun createElement(node: ASTNode?): PsiElement {
        val type = node!!.elementType
        if (type === ModElementTypes.DOCUMENT) {
            return YAMLDocumentImpl(node)
        }
        if (type === ModElementTypes.KEY_VALUE_PAIR) {
            return YAMLKeyValueImpl(node)
        }
        if (type === ModElementTypes.COMPOUND_VALUE) {
            return YAMLCompoundValueImpl(node)
        }
        if (type === ModElementTypes.SEQUENCE) {
            return YAMLBlockSequenceImpl(node)
        }
        if (type === ModElementTypes.MAPPING) {
            return ModBlockMappingImpl(node)
        }
        if (type === ModElementTypes.SEQUENCE_ITEM) {
            return YAMLSequenceItemImpl(node)
        }
        if (type === ModElementTypes.HASH) {
            return YAMLHashImpl(node)
        }
        if (type === ModElementTypes.ARRAY) {
            return YAMLArrayImpl(node)
        }
        if (type === ModElementTypes.SCALAR_LIST_VALUE) {
            return YAMLScalarListImpl(node)
        }
        if (type === ModElementTypes.SCALAR_TEXT_VALUE) {
            return ModPlainTextImpl(node)
        }
        if (type === ModElementTypes.SCALAR_PLAIN_VALUE) {
            return ModPlainTextImpl(node)
        }
        if (type === ModElementTypes.SCALAR_QUOTED_STRING) {
            return ModPlainTextImpl(node)
        }
        if (type === ModElementTypes.ANCHOR_NODE) {
            return YAMLAnchorImpl(node)
        }
        if (type === ModElementTypes.ALIAS_NODE) {
            return YAMLAliasImpl(node)
        }
        return YAMLPsiElementImpl(node)
    }

    override fun createParser(project: Project?): PsiParser {
        return ModParser()
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return ModFileImpl(viewProvider)
    }

    override fun getStringLiteralElements(): TokenSet {
        return ModElementTypes.TEXT_SCALAR_ITEMS
    }

    override fun getFileNodeType(): IFileElementType {
        return ModElementTypes.FILE
    }
}