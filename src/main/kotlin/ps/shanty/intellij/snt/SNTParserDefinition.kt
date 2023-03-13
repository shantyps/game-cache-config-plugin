package ps.shanty.intellij.snt

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiParser
import com.intellij.lang.properties.parsing.PropertiesParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import ps.shanty.intellij.snt.parser.SNTParser
import ps.shanty.intellij.snt.psi.impl.SNTFileImpl
import ps.shanty.intellij.snt.psi.impl.SNTEntryImpl
import ps.shanty.intellij.snt.psi.impl.SNTImpl

class SNTParserDefinition : PropertiesParserDefinition() {
    override fun getFileNodeType(): IFileElementType {
        return SNTElementTypes.FILE
    }

    override fun createParser(project: Project): PsiParser {
        return SNTParser()
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return SNTFileImpl(viewProvider)
    }

    override fun createElement(node: ASTNode): PsiElement {
        val type = node.elementType
        if (type === SNTElementTypes.PROPERTY) {
            return SNTEntryImpl(node)
        } else if (type === SNTElementTypes.PROPERTIES_LIST) {
            return SNTImpl(node)
        }
        throw AssertionError("Alien element type [$type]. Can't create Property PsiElement for that.")
    }
}