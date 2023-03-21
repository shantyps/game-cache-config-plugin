package ps.shanty.intellij.mod.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.util.containers.MultiMap
import org.jetbrains.annotations.Nls
import org.jetbrains.yaml.YAMLBundle
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YamlPsiElementVisitor
import ps.shanty.intellij.mod.ModBundle
import java.util.function.Consumer

class ModDuplicatedKeysInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : YamlPsiElementVisitor() {
            override fun visitMapping(mapping: YAMLMapping) {
                val occurrences = MultiMap<String, YAMLKeyValue>()
                for (keyValue in mapping.keyValues) {
                    val keyName = keyValue.keyText.trim { it <= ' ' }
                    // http://yaml.org/type/merge.html
                    if (keyName == "<<") {
                        continue
                    }
                    if (!keyName.isEmpty()) {
                        occurrences.putValue(keyName, keyValue)
                    }
                }
                for ((key, value) in occurrences.entrySet()) {
                    if (value.size > 1) {
                        value.forEach(Consumer { duplicatedKey: YAMLKeyValue ->
                            assert(duplicatedKey.key != null)
                            assert(duplicatedKey.parentMapping != null) { "This key is gotten from mapping" }
                            holder.registerProblem(
                                duplicatedKey.key!!,
                                ModBundle.message("ModDuplicatedKeysInspection.duplicated.key", key),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                RemoveDuplicatedKeyQuickFix(duplicatedKey)
                            )
                        })
                    }
                }
            }
        }
    }

    private class RemoveDuplicatedKeyQuickFix internal constructor(keyValue: YAMLKeyValue) : LocalQuickFix {
        private val myKeyValueHolder: SmartPsiElementPointer<YAMLKeyValue>

        init {
            myKeyValueHolder = SmartPointerManager.getInstance(keyValue.project).createSmartPsiElementPointer(keyValue)
        }

        override fun getFamilyName(): @Nls String {
            return ModBundle.message("ModDuplicatedKeysInspection.remove.key.quickfix.name")
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val keyVal = myKeyValueHolder.element
            if (keyVal == null || keyVal.parentMapping == null) {
                return
            }
            keyVal.parentMapping!!.deleteKeyValue(keyVal)
        }
    }
}