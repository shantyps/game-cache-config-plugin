package ps.shanty.intellij.snt.actions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.lang.properties.PropertiesBundle
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException
import ps.shanty.intellij.snt.SNTBundle
import ps.shanty.intellij.snt.SNTLanguage
import java.awt.datatransfer.StringSelection

class CopyPropertyKeyToClipboardIntention : IntentionAction, LowPriorityAction {
    override fun getText(): String {
        return familyName
    }

    override fun getFamilyName(): String {
        return SNTBundle.message("copy.property.key.to.clipboard.intention.family.name")
    }

    override fun isAvailable(
        project: Project,
        editor: Editor,
        file: PsiFile
    ): Boolean {
        return file.language.isKindOf(SNTLanguage.INSTANCE) &&
                CopyPropertyValueToClipboardIntention.getProperty(editor, file) != null
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val property =
            CopyPropertyValueToClipboardIntention.getProperty(editor, file)
                ?: return
        val key = property.unescapedKey ?: return
        CopyPasteManager.getInstance().setContents(StringSelection(key))
    }

    override fun startInWriteAction(): Boolean {
        return false
    }
}