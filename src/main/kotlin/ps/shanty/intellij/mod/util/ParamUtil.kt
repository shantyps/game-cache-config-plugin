package ps.shanty.intellij.mod.util

import com.intellij.openapi.project.Project
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YamlPsiElementVisitor
import ps.shanty.intellij.mod.ModFileExtension
import ps.shanty.intellij.mod.psi.ModFile
import ps.shanty.intellij.mod.util.ModUtil.findFiles

object ParamUtil {
    fun findParamType(project: Project, name: String): ModFileExtension? {
        val file = findFiles(project, name).firstOrNull() ?: return null
        val modFile = file as? ModFile ?: return null
        val document = modFile.documents.firstOrNull() ?: return null
        var paramType: ModFileExtension? = null
        document.acceptChildren(object : YamlPsiElementVisitor() {
            override fun visitMapping(mapping: YAMLMapping) {
                val type = mapping.getKeyValueByKey("type")?.value ?: return
                paramType = ModFileExtension.byType(type.text)
            }
        })
        return paramType
    }
}