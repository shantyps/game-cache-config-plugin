package ps.shanty.intellij.mod.resolve

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import ps.shanty.intellij.mod.Mod2KeyIndex
import ps.shanty.intellij.mod.util.ModUtil.findFiles
import ps.shanty.intellij.snt.SNTKeyIndex


internal class ModReference(
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
        results.addAll(multiResolveMod2NameReference(project))
        results.addAll(multiResolveSNTNameReference(project))
        return results.toTypedArray()
    }

    private fun multiResolveFileReference(project: Project): Array<ResolveResult> {
        val properties = findFiles(project, key)
        val results = arrayListOf<ResolveResult>()
        for (property in properties) {
            results.add(PsiElementResolveResult(property))
        }
        return results.toTypedArray()
    }

    private fun multiResolveMod2NameReference(project: Project): Array<ResolveResult> {
        val properties = Mod2KeyIndex.instance.get(key, project, GlobalSearchScope.allScope(project))
        val results = arrayListOf<ResolveResult>()
        for (property in properties) {
            results.add(PsiElementResolveResult(property))
        }
        return results.toTypedArray()
    }

    private fun multiResolveSNTNameReference(project: Project): Array<ResolveResult> {
        val properties = SNTKeyIndex.instance.get(key, project, GlobalSearchScope.allScope(project))
        val results = arrayListOf<ResolveResult>()
        for (property in properties) {
            results.add(PsiElementResolveResult(property))
        }
        return results.toTypedArray()
    }
}