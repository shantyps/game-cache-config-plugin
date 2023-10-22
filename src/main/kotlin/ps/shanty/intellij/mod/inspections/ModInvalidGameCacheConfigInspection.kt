package ps.shanty.intellij.mod.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.yaml.psi.YamlPsiElementVisitor
import ps.shanty.intellij.mod.ModFileExtension
import ps.shanty.intellij.mod.inspections.visitor.EnumVisitor
import ps.shanty.intellij.mod.inspections.visitor.StructVisitor
import ps.shanty.intellij.mod.util.PsiFileUtil.extension

class ModInvalidGameCacheConfigInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return when (holder.file.extension) {
            ModFileExtension.ENUM -> EnumVisitor(holder)
            ModFileExtension.STRUCT -> StructVisitor(holder)
            else -> object : YamlPsiElementVisitor() {} // blank visitor
        }
    }
}