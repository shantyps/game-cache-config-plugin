package ps.shanty.intellij.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.rpc.LOG
import org.jetbrains.yaml.psi.YAMLKeyValue
import ps.shanty.intellij.data.GameCacheConfigNames

class GameCacheConfigExportsStartupActivity : StartupActivity.Background {

    override fun runActivity(project: Project) {
        val references = hashMapOf<String, MutableList<PsiElement>>()

        loadConfigFile(project, references, "bas")
        loadConfigFile(project, references, "enums")
        loadConfigFile(project, references, "healthbars")
        loadConfigFile(project, references, "hitsplats")
        loadConfigFile(project, references, "invs")
        loadConfigFile(project, references, "kits")
        loadConfigFile(project, references, "locs")
        loadConfigFile(project, references, "mapfunctions")
        loadConfigFile(project, references, "npcs")
        loadConfigFile(project, references, "objs")
        loadConfigFile(project, references, "params")
        loadConfigFile(project, references, "seqs")
        loadConfigFile(project, references, "spotanims")
        loadConfigFile(project, references, "structs")
        loadConfigFile(project, references, "varbits")
        loadConfigFile(project, references, "vardoubles")
        loadConfigFile(project, references, "varlongs")
        loadConfigFile(project, references, "varps")
        loadConfigFile(project, references, "varstrings")

        GameCacheConfigNames.INSTANCE.configElements = references
    }

    private fun loadConfigFile(
        project: Project,
        references: MutableMap<String, MutableList<PsiElement>>,
        config: String,
    ) {
        ApplicationManager.getApplication().runReadAction {
            val files = FilenameIndex.getFilesByName(project, "$config.yaml", GlobalSearchScope.allScope(project))

            if (files.isEmpty()) {
                return@runReadAction
            }

            for (file in files) {
                val properties = PsiTreeUtil.collectElementsOfType(file, YAMLKeyValue::class.java)
                for (property in properties) {
                    if (property.keyText == "name") {
                        references.putIfAbsent(property.valueText, mutableListOf())
                        references[property.valueText]!!.add(property)
                    }
                }
            }
        }
    }
}