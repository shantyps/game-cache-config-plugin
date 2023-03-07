package ps.shanty.intellij.psi

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import ps.shanty.intellij.data.GameCacheConfigNames


internal class GameCacheConfigNameReference(
    element: PsiElement,
    textRange: TextRange,
) : PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {

    private val key = element.text.substring(textRange.startOffset, textRange.endOffset)

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        if (!GameCacheConfigNames.INSTANCE.configElements.containsKey(key)) {
            org.jetbrains.rpc.LOG.warn("return from $key")
            return ResolveResult.EMPTY_ARRAY
        }

        val properties = GameCacheConfigNames.INSTANCE.configElements[key]!!
        val results = arrayListOf<ResolveResult>()
        for (property in properties) {
            results.add(PsiElementResolveResult(property))
        }
        return results.toTypedArray()
    }
}