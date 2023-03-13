package ps.shanty.intellij.snt.formatter

import com.intellij.lang.Language
import com.intellij.lang.properties.PropertiesLanguage
import com.intellij.lang.properties.psi.impl.PropertyImpl
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.impl.PsiBasedStripTrailingSpacesFilter
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import ps.shanty.intellij.snt.SNTLanguage

class SNTStripTrailingSpacesFilterFactory : PsiBasedStripTrailingSpacesFilter.Factory() {
    override fun createFilter(document: Document): PsiBasedStripTrailingSpacesFilter {
        return object : PsiBasedStripTrailingSpacesFilter(document) {
            override fun process(psiFile: PsiFile) {
                object : PsiRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (element is PropertyImpl) {
                            val valueNode = element.valueNode
                            if (valueNode != null) {
                                disableRange(valueNode.textRange, true)
                            }
                        }
                        super.visitElement(element)
                    }
                }.visitElement(psiFile)
            }
        }
    }

    override fun isApplicableTo(language: Language): Boolean {
        return language.`is`(SNTLanguage.INSTANCE)
    }
}