package ps.shanty.intellij.mod.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.templateLanguages.OuterLanguageElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.yaml.YAMLBundle
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLValue
import org.jetbrains.yaml.psi.impl.YAMLBlockSequenceImpl
import ps.shanty.intellij.mod.psi.impl.ModBlockMappingImpl

class ModSameLineCompositeValueErrorAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is YAMLKeyValue) {
            return
        }
        val keyValue = element
        if (hasOuterElements(keyValue)) {
            return
        }
        val file = keyValue.containingFile ?: return
        val key = keyValue.key ?: return
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return
        val documentContent = document.charsSequence
        val value = keyValue.value
        if (value is ModBlockMappingImpl) {
            val firstSubValue = value.getFirstKeyValue()
            if (psiAreAtTheSameLine(key, firstSubValue, documentContent)) {
                reportAboutSameLine(holder, value)
            }
        }
        if (value is YAMLBlockSequenceImpl) {
            val items = value.items
            if (items.isEmpty()) {
                // a very strange situation: a sequence without any item
                return
            }
            val firstItem = items[0]
            if (psiAreAtTheSameLine(key, firstItem, documentContent)) {
                reportAboutSameLine(holder, value)
            }
        }
    }

    companion object {
        private fun reportAboutSameLine(holder: AnnotationHolder, value: YAMLValue) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                YAMLBundle.message("annotator.same.line.composed.value.message")
            ).range(value).create()
        }

        private fun psiAreAtTheSameLine(psi1: PsiElement, psi2: PsiElement, documentContent: CharSequence): Boolean {
            var leaf = PsiTreeUtil.nextLeaf(psi1)
            val lastLeaf = PsiTreeUtil.prevLeaf(psi2)
            while (leaf != null) {
                if (PsiUtilCore.getElementType(leaf) === YAMLTokenTypes.EOL) {
                    return false
                }
                if (leaf === lastLeaf) {
                    return true
                }
                leaf = PsiTreeUtil.nextLeaf(leaf)
            }
            // It is a kind of magic, normally we should return from the `while` above
            return false
        }

        private fun hasOuterElements(element: PsiElement): Boolean {
            val outerElements = PsiTreeUtil.findChildrenOfType(
                element,
                OuterLanguageElement::class.java
            )
            return !outerElements.isEmpty()
        }
    }
}