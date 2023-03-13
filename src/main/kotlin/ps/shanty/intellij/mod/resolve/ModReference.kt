package ps.shanty.intellij.mod.resolve

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import ps.shanty.intellij.mod.smart.ModQuoteHandler
import ps.shanty.intellij.snt.ShantyNameTableEntries


internal class ModReference(
    element: PsiElement,
    textRange: TextRange,
) : PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {

    private val key = ModQuoteHandler.removeQuotes(element.text.substring(textRange.startOffset, textRange.endOffset))

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = myElement.project
        val results = mutableListOf<ResolveResult>()
        results.addAll(multiResolveFileReference(project))
        if (results.isEmpty() && ShantyNameTableEntries.INSTANCE.tableEntryForName.containsKey(key)) {
            results.addAll(multiResolveNameReference())
        }
        return results.toTypedArray()
    }

    private fun multiResolveNameReference(): Array<ResolveResult> {
        val properties = ShantyNameTableEntries.INSTANCE.tableEntryForName[key]!!
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