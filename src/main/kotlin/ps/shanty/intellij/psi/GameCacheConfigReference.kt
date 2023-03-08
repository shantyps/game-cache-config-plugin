package ps.shanty.intellij.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import ps.shanty.intellij.data.GameCacheConfigNames


internal class GameCacheConfigReference(
    element: PsiElement,
    textRange: TextRange,
) : PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {

    private val key = element.text.substring(textRange.startOffset, textRange.endOffset)

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = myElement.project
        val results = mutableListOf<ResolveResult>()
        results.addAll(multiResolveFileReference(project))
        if (results.isEmpty() && GameCacheConfigNames.INSTANCE.configElements.containsKey(key)) {
            results.addAll(multiResolveNameReference())
        }
        return results.toTypedArray()
    }

    private fun multiResolveNameReference(): Array<ResolveResult> {
        val properties = GameCacheConfigNames.INSTANCE.configElements[key]!!
        val results = arrayListOf<ResolveResult>()
        for (property in properties) {
            results.add(PsiElementResolveResult(property))
        }
        return results.toTypedArray()
    }

    private fun multiResolveFileReference(project: Project): Array<ResolveResult> {
        val properties = FilenameIndex.getFilesByName(project, "$key.yaml", GlobalSearchScope.allScope(project))
        val results = arrayListOf<ResolveResult>()
        for (property in properties) {
            results.add(PsiElementResolveResult(property))
        }
        return results.toTypedArray()
    }
}