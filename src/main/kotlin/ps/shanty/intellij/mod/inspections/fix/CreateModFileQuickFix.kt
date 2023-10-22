package ps.shanty.intellij.mod.inspections.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.SmartPsiElementPointer
import org.jetbrains.annotations.Nls
import ps.shanty.intellij.PluginIcons
import ps.shanty.intellij.mod.ModBundle
import ps.shanty.intellij.mod.ModLanguage
import ps.shanty.intellij.mod.inspections.validator.CreateModFileHandler

class CreateModFileQuickFix(
    private val type: String,
    private val configName: String,
    private val folder: SmartPsiElementPointer<PsiDirectory>,
) : LocalQuickFix {
    override fun getFamilyName(): @Nls String {
        return ModBundle.message("ModInvalidGameCacheConfigInspection.create.file.quickfix.name", configName, type)
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val validator = CreateModFileHandler(project, folder.element!!)
        val completeFile = Messages.showInputDialog(
            project,
            "Enter the name of the $type file. You may use \\ or / to create folders.",
            "Create $type file",
            PluginIcons.LOGO,
            "$configName.$type",
            validator,
            TextRange.from(0, 0)
        ) ?: return

        ApplicationManager.getApplication().invokeLater({
            val file = PsiFileFactory.getInstance(project).createFileFromText(completeFile, ModLanguage.INSTANCE, "---\n")
            FileEditorManager.getInstance(project).openTextEditor(OpenFileDescriptor(project, file.virtualFile, 1, 0), true)
        }, ModalityState.NON_MODAL)
    }

    override fun startInWriteAction(): Boolean {
        return false
    }
}