package ps.shanty.intellij.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.rpc.LOG
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLKeyValue
import ps.shanty.intellij.listeners.GameCacheConfigReferenceContributor


object GameCacheConfigPluginUtils {
    /**
     * Searches the entire project for Simple language files with instances of the Simple property with the given key.
     *
     * @param project current project
     * @param key     to check
     * @return matching properties
     */
    fun findProperties(project: Project, key: String): List<PsiFile> {
        val result = arrayListOf<PsiFile>()
        val virtualFiles = FileTypeIndex.getFiles(YAMLFileType.YML, GlobalSearchScope.allScope(project))
        for (virtualFile in virtualFiles) {
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
            if (psiFile != null) {
                if (key == psiFile.name.substring(0, psiFile.name.indexOf("."))) {
                    result.add(psiFile)
                }
            }
        }
        return result
    }
}