package ps.shanty.intellij.mod.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import ps.shanty.intellij.mod.ModFileType

object ModUtil {
    fun findFiles(project: Project, key: String): List<PsiFile> {
        val files = mutableListOf<PsiFile>()
        ModFileType.ALL_EXTENSIONS.split(";").forEach { ext ->
            files.addAll(FilenameIndex.getFilesByName(project, "$key.$ext", GlobalSearchScope.allScope(project)))
        }
        return files
    }

    fun findPatchFolder(project: Project, folder: String): PsiDirectory {
        val folders = FilenameIndex.getFilesByName(project, folder, GlobalSearchScope.allScope(project), true)
        return folders.first { it.isDirectory && it.parent?.name == "patches" } as PsiDirectory
    }
}