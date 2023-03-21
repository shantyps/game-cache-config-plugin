package ps.shanty.intellij.mod.schema

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel
import com.intellij.json.JsonBundle
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker
import com.jetbrains.jsonSchema.impl.JsonComplianceCheckerOptions
import com.jetbrains.jsonSchema.impl.JsonSchemaComplianceChecker
import com.jetbrains.jsonSchema.impl.JsonSchemaObject
import org.jetbrains.yaml.YAMLBundle
import org.jetbrains.yaml.psi.YamlPsiElementVisitor
import org.jetbrains.yaml.schema.YamlJsonSchemaInspectionBase
import ps.shanty.intellij.mod.ModBundle
import javax.swing.JComponent

class ModJsonSchemaHighlightingInspection : YamlJsonSchemaInspectionBase() {
    var myCaseInsensitiveEnum = false
    override fun createOptionsPanel(): JComponent? {
        val optionsPanel = MultipleCheckboxOptionsPanel(this)
        optionsPanel.addCheckbox(
            JsonBundle.message("json.schema.inspection.case.insensitive.enum"),
            "myCaseInsensitiveEnum"
        )
        return optionsPanel
    }

    override fun doBuildVisitor(
        holder: ProblemsHolder,
        session: LocalInspectionToolSession,
        roots: Collection<PsiElement>,
        `object`: JsonSchemaObject
    ): PsiElementVisitor {
        val options = JsonComplianceCheckerOptions(myCaseInsensitiveEnum)
        return object : YamlPsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (!roots.contains(element)) return
                val walker = JsonLikePsiWalker.getWalker(element, `object`) ?: return
                val prefix = ModBundle.message("inspections.schema.validation.prefix") + " "
                JsonSchemaComplianceChecker(`object`, holder, walker, session, options, prefix).annotate(element)
            }
        }
    }
}