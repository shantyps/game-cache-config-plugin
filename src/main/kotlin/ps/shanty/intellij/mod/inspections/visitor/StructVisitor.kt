package ps.shanty.intellij.mod.inspections.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.DirtyUI
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YamlPsiElementVisitor
import ps.shanty.intellij.mod.ModBundle
import ps.shanty.intellij.mod.ModFileExtension
import ps.shanty.intellij.mod.inspections.fix.CreateModFileQuickFix
import ps.shanty.intellij.mod.util.ModUtil.findPatchFolder
import ps.shanty.intellij.mod.util.ParamUtil
import ps.shanty.intellij.snt.SNTKeyIndex

class StructVisitor(private val holder: ProblemsHolder) : YamlPsiElementVisitor() {
    @DirtyUI
    override fun visitMapping(mapping: YAMLMapping) {
        val params = mapping.getKeyValueByKey("params")?.value ?: return

        if (params !is YAMLMapping) {
            return
        }

        for (keyValue in params.keyValues) {
            val keyEntries = SNTKeyIndex.instance.get(keyValue.keyText, mapping.project, GlobalSearchScope.allScope(mapping.project))

            if (keyEntries.isEmpty()) {
                val folder = findPatchFolder(mapping.project, ModFileExtension.PARAM.patchFolder)
                val smartFolder = SmartPointerManager.getInstance(mapping.project).createSmartPsiElementPointer(folder)
                holder.registerProblem(
                    keyValue.key!!,
                    ModBundle.message("ModInvalidGameCacheConfigInspection.no.snt.entry", keyValue.keyText),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    CreateModFileQuickFix(ModFileExtension.PARAM.extension, keyValue.keyText, smartFolder)
                )
            }

            val extension = ParamUtil.findParamType(mapping.project, keyValue.keyText) ?: continue
            val valueText = keyValue.value?.text?.replace("\"", "") ?: continue
            val valueEntries = SNTKeyIndex.instance.get(valueText, mapping.project, GlobalSearchScope.allScope(mapping.project))
            if (valueEntries.isEmpty() && keyValue.value != null) {
                val folder = findPatchFolder(mapping.project, extension.patchFolder)
                val smartFolder = SmartPointerManager.getInstance(mapping.project).createSmartPsiElementPointer(folder)
                holder.registerProblem(
                    keyValue.value!!,
                    ModBundle.message("ModInvalidGameCacheConfigInspection.no.snt.entry", valueText),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    CreateModFileQuickFix(extension.extension, valueText, smartFolder)
                )
            }
        }
    }
}