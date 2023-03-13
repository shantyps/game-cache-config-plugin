package ps.shanty.intellij.snt.actions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.lang.properties.psi.Property
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import ps.shanty.intellij.snt.SNTBundle
import ps.shanty.intellij.snt.SNTLanguage
import ps.shanty.intellij.snt.psi.SNTEntry
import java.awt.datatransfer.StringSelection

class CopyPropertyValueToClipboardIntention : IntentionAction, LowPriorityAction {
    override fun getText(): String {
        return familyName
    }

    override fun getFamilyName(): String {
        return SNTBundle.message("copy.property.value.to.clipboard.intention.family.name")
    }

    override fun isAvailable(
        project: Project,
        editor: Editor,
        file: PsiFile
    ): Boolean {
        return file.language.isKindOf(SNTLanguage.INSTANCE) &&
                getProperty(editor, file) != null
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val property = getProperty(editor, file)
            ?: return
        val value = property.unescapedValue ?: return
        CopyPasteManager.getInstance().setContents(StringSelection(value))
    }

    override fun startInWriteAction(): Boolean {
        return false
    }

    companion object {
        fun getProperty(editor: Editor, file: PsiFile): SNTEntry? {
            val offset = editor.caretModel.offset
            return PsiTreeUtil.getParentOfType(
                file.findElementAt(offset),
                SNTEntry::class.java
            )
        }
    }
}