package ps.shanty.intellij.snt.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.psi.formatter.PsiBasedFormattingModel
import com.intellij.psi.impl.source.SourceTreeToPsiMap
import com.intellij.psi.impl.source.tree.TreeElement
import com.intellij.psi.impl.source.tree.TreeUtil

class SNTFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val root: ASTNode =
            TreeUtil.getFileElement((SourceTreeToPsiMap.psiElementToTree(formattingContext.psiElement) as TreeElement?)!!)
        val containingFile = formattingContext.containingFile
        val documentModel = FormattingDocumentModelImpl.createOn(containingFile)
        return PsiBasedFormattingModel(
            containingFile, SNTRootBlock(root, formattingContext.codeStyleSettings),
            documentModel
        )
    }
}