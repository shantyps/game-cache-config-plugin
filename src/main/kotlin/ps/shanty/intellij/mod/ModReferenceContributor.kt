package ps.shanty.intellij.mod

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import ps.shanty.intellij.mod.resolve.ModReference


internal class ModReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(), GameCacheConfigReferenceProvider())
    }

    private class GameCacheConfigReferenceProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
            val text = element.text

            val words = text.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (words.size != 1) {
                return PsiReference.EMPTY_ARRAY
            }
            var range = TextRange.allOf(words[0])
            if (words[0].startsWith("\"") && words[0].endsWith("\"")) {
                range = TextRange.create(1, words[0].length - 1)
            }
            return arrayOf(ModReference(element, range))
        }
    }
}