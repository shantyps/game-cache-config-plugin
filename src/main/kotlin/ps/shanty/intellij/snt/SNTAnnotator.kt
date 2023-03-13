package ps.shanty.intellij.snt

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.ASTNode
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.properties.*
import com.intellij.lang.properties.editor.PropertiesValueHighlighter
import com.intellij.lang.properties.psi.Property
import com.intellij.lang.properties.psi.impl.PropertyImpl
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import ps.shanty.intellij.snt.formatter.SNTHighlighter
import ps.shanty.intellij.snt.psi.SNTEntry
import ps.shanty.intellij.snt.psi.impl.SNTEntryImpl

class SNTAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is SNTEntry) return
        val property = element
        val propertiesFile = property.sntFile
        val key = property.unescapedKey ?: return
        val others: Collection<ISNTEntry> = propertiesFile.findPropertiesByKey(key)
        val keyNode = (property as SNTEntryImpl).keyNode ?: return
        if (others.size != 1 &&
            EP_NAME.findFirstSafe { suppressor ->
                suppressor.suppressAnnotationFor(
                    property
                )
            } == null
        ) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                SNTBundle.message("duplicate.property.key.error.message")
            ).range(keyNode).create()
        }
        highlightTokens(property, keyNode, holder, PropertiesHighlighter())
        val valueNode = property.valueNode
        if (valueNode != null) {
            highlightTokens(property, valueNode, holder, PropertiesValueHighlighter())
        }
    }

    companion object {
        private val EP_NAME =
            ExtensionPointName.create<DuplicateSNTEntryKeyAnnotationSuppressor>("ps.shanty.intellij.duplicateSNTEntryKeyAnnotationSuppressor")

        private fun highlightTokens(
            property: SNTEntry,
            node: ASTNode,
            holder: AnnotationHolder,
            highlighter: PropertiesHighlighter
        ) {
            val lexer = highlighter.highlightingLexer
            val s = node.text
            lexer.start(s)
            while (lexer.tokenType != null) {
                val elementType = lexer.tokenType
                val keys = highlighter.getTokenHighlights(elementType)
                for (key in keys) {
                    val displayName = SNTHighlighter.SNTComponent.getDisplayName(key)
                    val severity = SNTHighlighter.SNTComponent.getSeverity(key)
                    if (severity != null && displayName != null) {
                        val start = lexer.tokenStart + node.textRange.startOffset
                        val end = lexer.tokenEnd + node.textRange.startOffset
                        val textRange = TextRange(start, end)
                        val attributes = EditorColorsManager.getInstance().globalScheme.getAttributes(key)
                        var builder = holder.newAnnotation(severity, displayName).range(textRange)
                            .enforcedTextAttributes(attributes)
                        if (key === PropertiesHighlighter.PropertiesComponent.PROPERTIES_INVALID_STRING_ESCAPE.textAttributesKey) {
                            builder = builder.withFix(object : IntentionAction {
                                override fun getText(): String {
                                    return PropertiesBundle.message("unescape")
                                }

                                override fun getFamilyName(): String {
                                    return text
                                }

                                override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
                                    if (!property.isValid || !property.manager.isInProject(property)) return false
                                    val text = property.sntFile.getContainingFile().text
                                    val startOffset = textRange.startOffset
                                    return text.length > startOffset && text[startOffset] == '\\'
                                }

                                override fun invoke(project: Project, editor: Editor, file: PsiFile) {
                                    val offset = textRange.startOffset
                                    if (property.sntFile.getContainingFile().text[offset] == '\\') {
                                        editor.document.deleteString(offset, offset + 1)
                                    }
                                }

                                override fun startInWriteAction(): Boolean {
                                    return true
                                }
                            })
                        }
                        builder.create()
                    }
                }
                lexer.advance()
            }
        }
    }
}