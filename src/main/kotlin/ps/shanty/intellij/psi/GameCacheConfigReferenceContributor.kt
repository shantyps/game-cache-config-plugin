package ps.shanty.intellij.psi

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext


internal class GameCacheConfigReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(), GameCacheConfigFileReferenceProvider(), PsiReferenceRegistrar.DEFAULT_PRIORITY)

        registrar.registerReferenceProvider(PlatformPatterns.psiElement(), GameCacheConfigNameReferenceProvider(), PsiReferenceRegistrar.LOWER_PRIORITY)
    }

    private class GameCacheConfigFileReferenceProvider : PsiReferenceProvider() {

        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
            val text = element.text

            val words = text.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (words.size != 1) {
                return PsiReference.EMPTY_ARRAY
            }
            return arrayOf(GameCacheConfigFileReference(element, TextRange.allOf(words[0])))
        }
    }

    private class GameCacheConfigNameReferenceProvider : PsiReferenceProvider() {

        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
            val text = element.text

            val words = text.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (words.size != 1) {
                return PsiReference.EMPTY_ARRAY
            }
            return arrayOf(GameCacheConfigNameReference(element, TextRange.allOf(words[0])))
        }
    }
}