package ps.shanty.intellij.mod.schema

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.jetbrains.jsonSchema.ide.JsonSchemaService
import com.jetbrains.jsonSchema.impl.JsonSchemaCompletionContributor
import org.jetbrains.yaml.psi.YAMLDocument
import ps.shanty.intellij.mod.psi.impl.ModBlockMappingImpl
import ps.shanty.intellij.mod.psi.impl.ModKeyValueImpl
import ps.shanty.intellij.mod.psi.impl.ModPlainTextImpl
import ps.shanty.intellij.snt.SNTKeyIndex


class ModJsonSchemaCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters?>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val position = parameters.originalPosition ?: return
                    val fileName = position.containingFile.name
                    val fileExtension = fileName.substring(fileName.indexOf(".") + 1)
                    when (fileExtension.lowercase()) {
                        "struct" -> {
                            addStructCompletions(position, result)
                        }
                    }
                }
            }
        )
    }

    private fun addStructCompletions(position: PsiElement, result: CompletionResultSet) {
        if (position.isNotStructParamKey()) {
            return
        }
        SNTKeyIndex.instance.processAllKeysInSNT(position.project, "param") {
            if (it.contains(position.text)) {
                result.addElement(LookupElementBuilder.create(it))
            }
            return@processAllKeysInSNT true
        }
    }

    private fun PsiElement.isNotStructParamKey(): Boolean {
        return !isStructParamKey()
    }

    private fun PsiElement.isStructParamKey(): Boolean {
        return (parent is ModPlainTextImpl &&
                parent.parent is ModBlockMappingImpl &&
                parent.parent.parent is ModKeyValueImpl &&
                parent.parent.parent.parent is ModBlockMappingImpl &&
                parent.parent.parent.parent.parent is YAMLDocument)
                ||
                (parent is ModPlainTextImpl &&
                        parent.parent is ModKeyValueImpl &&
                        parent.parent.parent is ModBlockMappingImpl &&
                        parent.parent.parent.parent is YAMLDocument)
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val position = parameters.position
        val jsonSchemaService = JsonSchemaService.Impl.get(position.project)
        val jsonSchemaObject = jsonSchemaService.getSchemaObject(parameters.originalFile)
        if (jsonSchemaObject != null) {
            JsonSchemaCompletionContributor.doCompletion(parameters, result, jsonSchemaObject, false)
        } else {
            super.fillCompletionVariants(parameters, result)
        }
    }
}
