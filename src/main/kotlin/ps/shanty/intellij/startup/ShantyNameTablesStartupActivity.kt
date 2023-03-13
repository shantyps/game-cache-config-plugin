package ps.shanty.intellij.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import ps.shanty.intellij.data.ShantyNameTableEntries
import ps.shanty.intellij.parser.GameCacheConfigKeyValue

class ShantyNameTablesStartupActivity : StartupActivity.DumbAware {

    override fun runActivity(project: Project) {
        val references = hashMapOf<String, MutableList<PsiElement>>()

        loadConfigFile(project, references, "bas")
        loadConfigFile(project, references, "category")
        loadConfigFile(project, references, "chat")
        loadConfigFile(project, references, "enum")
        loadConfigFile(project, references, "gender")
        loadConfigFile(project, references, "healthbar")
        loadConfigFile(project, references, "hitsplat")
        loadConfigFile(project, references, "interface")
        loadConfigFile(project, references, "interface_position")
        loadConfigFile(project, references, "inv")
        loadConfigFile(project, references, "jingle")
        loadConfigFile(project, references, "kit")
        loadConfigFile(project, references, "loc")
        loadConfigFile(project, references, "mapfunction")
        loadConfigFile(project, references, "minimap")
        loadConfigFile(project, references, "model")
        loadConfigFile(project, references, "musictrack")
        loadConfigFile(project, references, "npc")
        loadConfigFile(project, references, "obj")
        loadConfigFile(project, references, "param")
        loadConfigFile(project, references, "prayericon")
        loadConfigFile(project, references, "seq")
        loadConfigFile(project, references, "skullicon")
        loadConfigFile(project, references, "soundsynth")
        loadConfigFile(project, references, "spotanim")
        loadConfigFile(project, references, "sprite")
        loadConfigFile(project, references, "struct")
        loadConfigFile(project, references, "varbit")
        loadConfigFile(project, references, "varcint")
        loadConfigFile(project, references, "vardouble")
        loadConfigFile(project, references, "varlong")
        loadConfigFile(project, references, "varp")
        loadConfigFile(project, references, "varstring")

        ShantyNameTableEntries.INSTANCE.tableEntryForName = references
    }

    private fun loadConfigFile(
        project: Project,
        references: MutableMap<String, MutableList<PsiElement>>,
        config: String,
    ) {
        ApplicationManager.getApplication().runReadAction {
            val files = FilenameIndex.getFilesByName(project, "$config.snt", GlobalSearchScope.allScope(project))

            if (files.isEmpty()) {
                return@runReadAction
            }

            for (file in files) {
                val properties = PsiTreeUtil.collectElementsOfType(file, GameCacheConfigKeyValue::class.java)
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