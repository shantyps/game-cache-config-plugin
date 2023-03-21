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
import ps.shanty.intellij.mod.ModFileExtension
import ps.shanty.intellij.mod.inspections.visitor.EnumVisitor
import ps.shanty.intellij.mod.util.PsiFileUtil.extension
import java.util.function.Consumer

class ModInvalidGameCacheConfigInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return when (holder.file.extension) {
            ModFileExtension.ENUM -> EnumVisitor(holder)
            else -> object : YamlPsiElementVisitor() {} // blank visitor
        }
    }
}