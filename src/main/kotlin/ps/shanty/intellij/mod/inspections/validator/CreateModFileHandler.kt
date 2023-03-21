package ps.shanty.intellij.mod.inspections.validator

import com.intellij.CommonBundle
import com.intellij.history.LocalHistory
import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.CreateElementActionBase
import com.intellij.ide.actions.CreateFileAction.MkDirs
import com.intellij.ide.util.DirectoryUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.UnknownFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsContexts.DetailedDescription
import com.intellij.openapi.util.NlsContexts.DialogMessage
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileSystemItem
import com.intellij.util.IncorrectOperationException
import ps.shanty.intellij.mod.ModBundle
import java.awt.Component
import java.io.File
import java.util.*

class CreateModFileHandler(
    private val project: Project,
    private val directory: PsiDirectory,
) : InputValidatorEx {
    var createdElement: PsiFileSystemItem? = null
        private set
    private var myErrorText: @DetailedDescription String? = null
    override fun checkInput(inputString: String): Boolean {
        val tokenizer = StringTokenizer(inputString, "\\/")
        var vFile: VirtualFile? = directory.virtualFile
        var firstToken = true
        while (tokenizer.hasMoreTokens()) {
            val token = tokenizer.nextToken()
            if (!tokenizer.hasMoreTokens() && (token == "." || token == "..")) {
                myErrorText = IdeBundle.message("error.invalid.directory.name", token)
                return false
            }
            if (vFile != null) {
                if (firstToken && "~" == token) {
                    val userHomeDir = VfsUtil.getUserHomeDir()
                    if (userHomeDir == null) {
                        myErrorText = IdeBundle.message("error.user.home.directory.not.found")
                        return false
                    }
                    vFile = userHomeDir
                } else if (".." == token) {
                    val parent = vFile.parent
                    if (parent == null) {
                        myErrorText = IdeBundle.message(
                            "error.invalid.directory",
                            vFile.presentableUrl + File.separatorChar + ".."
                        )
                        return false
                    }
                    vFile = parent
                } else if ("." != token) {
                    val child = vFile.findChild(token)
                    if (child != null) {
                        if (!child.isDirectory) {
                            myErrorText = IdeBundle.message("error.file.with.name.already.exists", token)
                            return false
                        } else if (!tokenizer.hasMoreTokens()) {
                            myErrorText = IdeBundle.message("error.directory.with.name.already.exists", token)
                            return false
                        }
                    }
                    vFile = child
                }
            }
            if (FileTypeManager.getInstance().isFileIgnored(token)) {
                myErrorText = IdeBundle.message("warning.create.directory.with.ignored.name", token)
                return true
            }
            firstToken = false
        }
        myErrorText = null
        return true
    }

    override fun getErrorText(inputString: String): String? {
        return myErrorText
    }

    override fun canClose(subDirName: String): Boolean {
        if (subDirName.length == 0) {
            showErrorDialog(IdeBundle.message("error.name.should.be.specified"))
            return false
        }
        val multiCreation = StringUtil.containsAnyChar(subDirName, "\\/")
        if (!multiCreation) {
            try {
                directory.checkCreateSubdirectory(subDirName)
            } catch (ex: IncorrectOperationException) {
                showErrorDialog(CreateElementActionBase.filterMessage(ex.message))
                return false
            }
        }
        doCreateElement(subDirName)
        return createdElement != null
    }

    private fun doCreateElement(subDirName: String) {
        val command = Runnable {
            val run = Runnable {
                val actionName = ModBundle.message("progress.creating.mod.file", subDirName)
                val action =
                    LocalHistory.getInstance().startAction(actionName)
                try {
                    val mkdirs = MkDirs(subDirName, directory)
                    createdElement = mkdirs.directory.createFile(mkdirs.newName)
                } catch (ex: IncorrectOperationException) {
                    ApplicationManager.getApplication().invokeLater {
                        showErrorDialog(
                            CreateElementActionBase.filterMessage(
                                ex.message
                            )
                        )
                    }
                } finally {
                    action.finish()
                }
            }
            ApplicationManager.getApplication().runWriteAction(run)
        }
        CommandProcessor.getInstance().executeCommand(
            project,
            command,
            IdeBundle.message("command.create.file"),
            null
        )
    }

    private fun showErrorDialog(message: @DialogMessage String?) {
        val title = CommonBundle.getErrorTitle()
        val icon = Messages.getErrorIcon()
        Messages.showMessageDialog(project, message, title, icon)
    }
}