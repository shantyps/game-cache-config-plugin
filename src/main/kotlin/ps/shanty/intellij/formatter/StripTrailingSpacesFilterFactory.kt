package ps.shanty.intellij.formatter

import com.intellij.lang.Language
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.impl.PsiBasedStripTrailingSpacesFilter
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor
import ps.shanty.intellij.parser.GameCacheConfigLanguage

private class GameCacheConfigStripTrailingSpacesFilterFactory : PsiBasedStripTrailingSpacesFilter.Factory() {
    override fun createFilter(document: Document): PsiBasedStripTrailingSpacesFilter = object : PsiBasedStripTrailingSpacesFilter(document) {
        override fun process(psiFile: PsiFile) {
            psiFile.accept(object : YamlRecursivePsiElementVisitor(){
                override fun visitScalar(scalar: YAMLScalar) {
                    disableRange(scalar.textRange, false)
                    super.visitScalar(scalar)
                }
            })
        }
    }

    override fun isApplicableTo(language: Language): Boolean = language.`is`(GameCacheConfigLanguage.INSTANCE)
}