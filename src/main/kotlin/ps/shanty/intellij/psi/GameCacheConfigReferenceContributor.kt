package ps.shanty.intellij.psi

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext


internal class GameCacheConfigReferenceContributor : PsiReferenceContributor() {

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
            return arrayOf(GameCacheConfigReference(element, TextRange.allOf(words[0])))
        }
    }
}