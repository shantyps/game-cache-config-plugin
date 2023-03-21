package ps.shanty.intellij.mod.schema

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker
import com.jetbrains.jsonSchema.impl.JsonSchemaObject
import com.jetbrains.jsonSchema.impl.JsonSchemaResolver
import org.jetbrains.yaml.YAMLBundle
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YamlPsiElementVisitor
import org.jetbrains.yaml.schema.YamlJsonSchemaInspectionBase
import ps.shanty.intellij.mod.ModBundle

class ModJsonSchemaDeprecationInspection : YamlJsonSchemaInspectionBase() {
    override fun doBuildVisitor(
        holder: ProblemsHolder,
        session: LocalInspectionToolSession,
        roots: Collection<PsiElement>,
        schema: JsonSchemaObject
    ): PsiElementVisitor {
        val sampleElement = roots.iterator().next()
        val walker = JsonLikePsiWalker.getWalker(sampleElement, schema)
        if (walker == null || schema == null) {
            return PsiElementVisitor.EMPTY_VISITOR
        }
        val project = sampleElement.project
        return object : YamlPsiElementVisitor() {
            override fun visitKeyValue(keyValue: YAMLKeyValue) {
                annotate(keyValue)
                super.visitKeyValue(keyValue)
            }

            private fun annotate(keyValue: YAMLKeyValue) {
                val key = keyValue.key ?: return
                val position = walker.findPosition(keyValue, true) ?: return
                val result = JsonSchemaResolver(project, schema, position).detailedResolve()
                for (`object` in result.mySchemas) {
                    val message = `object`.deprecationMessage
                    if (message != null) {
                        holder.registerProblem(
                            key,
                            ModBundle.message("inspections.schema.deprecation.text", keyValue.name, message)
                        )
                        return
                    }
                }
            }
        }
    }
}