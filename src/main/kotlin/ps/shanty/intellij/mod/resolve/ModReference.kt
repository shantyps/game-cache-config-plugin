package ps.shanty.intellij.mod.resolve

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import ps.shanty.intellij.mod.smart.ModQuoteHandler
import ps.shanty.intellij.mod.util.ModUtil.findFiles
import ps.shanty.intellij.snt.SNTEntries


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
        if (results.isEmpty() && SNTEntries.INSTANCE.tableEntryForName.containsKey(key)) {
            results.addAll(multiResolveNameReference())
        }
        return results.toTypedArray()
    }

    private fun multiResolveNameReference(): Array<ResolveResult> {
        val properties = SNTEntries.INSTANCE.tableEntryForName[key]!!
        val results = arrayListOf<ResolveResult>()
        for (property in properties) {
            results.add(PsiElementResolveResult(property))
        }
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
}