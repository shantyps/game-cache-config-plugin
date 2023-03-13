package ps.shanty.intellij.mod

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameDialog
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import ps.shanty.intellij.mod.psi.ModFile

class RenameModFileProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(element: PsiElement): Boolean {
        return element is ModFile
    }

    override fun createRenameDialog(
        project: Project,
        element: PsiElement,
        nameSuggestionContext: PsiElement?,
        editor: Editor?
    ): RenameDialog {
        // setting editor to null invokes "selectNameWithoutExtension"
        return super.createRenameDialog(project, element, nameSuggestionContext, null)
    }
}