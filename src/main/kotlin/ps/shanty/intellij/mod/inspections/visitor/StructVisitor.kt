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
            val keyText = keyValue.key?.text?.replace("\"", "") ?: continue
            val keyEntries = SNTKeyIndex.instance.get(keyText, mapping.project, GlobalSearchScope.allScope(mapping.project))

            if (keyEntries.isEmpty()) {
                val folder = findPatchFolder(mapping.project, ModFileExtension.PARAM.patchFolder)
                val smartFolder = SmartPointerManager.getInstance(mapping.project).createSmartPsiElementPointer(folder)
                holder.registerProblem(
                    keyValue.key!!,
                    ModBundle.message("ModInvalidGameCacheConfigInspection.no.snt.entry", keyText, ModFileExtension.PARAM.extensions, ModFileExtension.PARAM.sntName),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    CreateModFileQuickFix(ModFileExtension.PARAM.extensions.last(), keyText, smartFolder)
                )
            }

            val extension = ParamUtil.findParamType(mapping.project, keyText) ?: continue
            val valueText = keyValue.value?.text?.replace("\"", "") ?: continue
            val valueEntries = SNTKeyIndex.instance.get(valueText, mapping.project, GlobalSearchScope.allScope(mapping.project))
            if (valueEntries.isEmpty() && keyValue.value != null) {
                val folder = findPatchFolder(mapping.project, extension.patchFolder)
                val smartFolder = SmartPointerManager.getInstance(mapping.project).createSmartPsiElementPointer(folder)
                holder.registerProblem(
                    keyValue.value!!,
                    ModBundle.message("ModInvalidGameCacheConfigInspection.no.snt.entry", valueText, extension.extensions, extension.sntName),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    CreateModFileQuickFix(extension.extensions.last(), valueText, smartFolder)
                )
            }
        }
    }
}