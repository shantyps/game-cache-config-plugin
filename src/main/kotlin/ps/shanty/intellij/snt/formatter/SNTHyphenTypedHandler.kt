package ps.shanty.intellij.snt.formatter

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.editor.impl.TypedActionImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.YAMLSequence
import ps.shanty.intellij.snt.SNTElementTypes
import ps.shanty.intellij.snt.psi.SNTFile

class SNTHyphenTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        autoIndentHyphen(c, project, editor, file)
        return Result.CONTINUE
    }

    companion object {
        private fun autoIndentHyphen(
            c: Char,
            project: Project,
            editor: Editor,
            file: PsiFile
        ) {
            if (!(c == ' ' && file is SNTFile)) {
                return
            }
            if (!file.isValid()) {
                return
            }
            val curPosOffset = editor.caretModel.offset
            if (curPosOffset < 2) {
                return
            }
            val offset = curPosOffset - 2
            val document = editor.document
            if (document.charsSequence[offset] != '-') {
                return
            }
            if (curPosOffset < document.textLength && document.charsSequence[curPosOffset] != '\n') {
                return
            }
            PsiDocumentManager.getInstance(project).commitDocument(document)
            val element = file.findElementAt(offset)
            if (PsiUtilCore.getElementType(element) !== YAMLTokenTypes.SEQUENCE_MARKER) {
                return
            }
            val item = element!!.parent
            if (PsiUtilCore.getElementType(item) !== SNTElementTypes.SEQUENCE_ITEM) {
                // Should not be possible now
                return
            }
            val sequence = item.parent
            if (PsiUtilCore.getElementType(sequence) !== SNTElementTypes.SEQUENCE) {
                // It could be some composite component (with syntax error)
                return
            }
            if ((sequence as YAMLSequence).items.size != 1) {
                return
            }
            val handler = (TypedAction.getInstance() as TypedActionImpl).defaultRawTypedHandler
            handler.beginUndoablePostProcessing()
            ApplicationManager.getApplication().runWriteAction {
                val newOffset = CodeStyleManager.getInstance(project).adjustLineIndent(file, offset)
                editor.caretModel.moveToOffset(newOffset + 2)
            }
        }
    }
}