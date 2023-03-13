package ps.shanty.intellij.snt

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
import ps.shanty.intellij.snt.parser.SNTParser
import ps.shanty.intellij.snt.psi.impl.SNTFileImpl

class SNTParserDefinition : YAMLParserDefinition() {

    override fun createElement(node: ASTNode?): PsiElement {
        val type = node!!.elementType
        if (type === SNTElementTypes.DOCUMENT) {
            return YAMLDocumentImpl(node)
        }
        if (type === SNTElementTypes.KEY_VALUE_PAIR) {
            return YAMLKeyValueImpl(node)
        }
        if (type === SNTElementTypes.COMPOUND_VALUE) {
            return YAMLCompoundValueImpl(node)
        }
        if (type === SNTElementTypes.SEQUENCE) {
            return YAMLBlockSequenceImpl(node)
        }
        if (type === SNTElementTypes.MAPPING) {
            return YAMLBlockMappingImpl(node)
        }
        if (type === SNTElementTypes.SEQUENCE_ITEM) {
            return YAMLSequenceItemImpl(node)
        }
        if (type === SNTElementTypes.HASH) {
            return YAMLHashImpl(node)
        }
        if (type === SNTElementTypes.ARRAY) {
            return YAMLArrayImpl(node)
        }
        if (type === SNTElementTypes.SCALAR_LIST_VALUE) {
            return YAMLScalarListImpl(node)
        }
        if (type === SNTElementTypes.SCALAR_TEXT_VALUE) {
            return YAMLScalarTextImpl(node)
        }
        if (type === SNTElementTypes.SCALAR_PLAIN_VALUE) {
            return YAMLPlainTextImpl(node)
        }
        if (type === SNTElementTypes.SCALAR_QUOTED_STRING) {
            return YAMLQuotedTextImpl(node)
        }
        if (type === SNTElementTypes.ANCHOR_NODE) {
            return YAMLAnchorImpl(node)
        }
        if (type === SNTElementTypes.ALIAS_NODE) {
            return YAMLAliasImpl(node)
        }
        return YAMLPsiElementImpl(node)
    }

    override fun createParser(project: Project?): PsiParser {
        return SNTParser()
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return SNTFileImpl(viewProvider)
    }

    override fun getStringLiteralElements(): TokenSet {
        return SNTElementTypes.TEXT_SCALAR_ITEMS
    }

    override fun getFileNodeType(): IFileElementType {
        return SNTElementTypes.FILE
    }
}