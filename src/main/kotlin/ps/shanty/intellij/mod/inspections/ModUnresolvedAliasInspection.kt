package ps.shanty.intellij.mod.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import org.jetbrains.yaml.YAMLBundle
import org.jetbrains.yaml.psi.YAMLAlias
import org.jetbrains.yaml.psi.YamlPsiElementVisitor
import ps.shanty.intellij.mod.ModBundle

class ModUnresolvedAliasInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : YamlPsiElementVisitor() {
            override fun visitAlias(alias: YAMLAlias) {
                val reference: PsiReference? = alias.reference
                if (reference != null && reference.resolve() == null) {
                    holder.registerProblem(
                        reference,
                        ModBundle.message("inspections.unresolved.alias.message", alias.aliasName),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    )
                }
            }
        }
    }
}