package ps.shanty.intellij.mod.inspections.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.DirtyUI
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YamlPsiElementVisitor
import ps.shanty.intellij.mod.ModBundle
import ps.shanty.intellij.mod.ModFileExtension
import ps.shanty.intellij.mod.inspections.fix.CreateModFileQuickFix
import ps.shanty.intellij.mod.util.ModUtil.findPatchFolder
import ps.shanty.intellij.snt.SNTKeyIndex

class EnumVisitor(private val holder: ProblemsHolder) : YamlPsiElementVisitor() {
    @DirtyUI
    override fun visitMapping(mapping: YAMLMapping) {
        val keyType = mapping.getKeyValueByKey("keyType")?.valueText ?: return
        val valueType = mapping.getKeyValueByKey("valueType")?.valueText ?: return
        val params = mapping.getKeyValueByKey("params")?.value ?: return

        if (params !is YAMLMapping) {
            return
        }

        for (keyValue in params.keyValues) {
            val keyEntries = SNTKeyIndex.instance.get(keyValue.keyText, mapping.project, GlobalSearchScope.allScope(mapping.project))
            val valueEntries = SNTKeyIndex.instance.get(keyValue.valueText, mapping.project, GlobalSearchScope.allScope(mapping.project))

            if (keyEntries.isEmpty()) {
                holder.registerProblem(
                    keyValue.key!!,
                    ModBundle.message("ModInvalidGameCacheConfigInspection.no.snt.entry", keyValue.keyText),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                )
            }

            if (valueEntries.isEmpty()) {
                holder.registerProblem(
                    keyValue.value!!,
                    ModBundle.message("ModInvalidGameCacheConfigInspection.no.snt.entry", keyValue.valueText),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                )
            }
        }
    }
}