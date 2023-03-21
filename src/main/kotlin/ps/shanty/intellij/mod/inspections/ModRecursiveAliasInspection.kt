package ps.shanty.intellij.mod.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.YAMLBundle
import org.jetbrains.yaml.psi.YAMLAlias
import org.jetbrains.yaml.psi.YamlPsiElementVisitor
import ps.shanty.intellij.mod.ModBundle

class ModRecursiveAliasInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : YamlPsiElementVisitor() {
            override fun visitAlias(alias: YAMLAlias) {
                val reference = alias.reference
                val anchor = reference?.resolve()
                val value = anchor?.markedValue ?: return
                if (PsiTreeUtil.isAncestor(value, alias.parent, false)) {
                    holder.registerProblem(
                        reference,
                        ModBundle.message("inspections.recursive.alias.message"),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    )
                }
            }
        }
    }
}