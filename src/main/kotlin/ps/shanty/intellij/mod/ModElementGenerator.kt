package ps.shanty.intellij.mod

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.LocalTimeCounter
import org.jetbrains.yaml.YAMLTextUtil
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.*
import org.jetbrains.yaml.psi.impl.YAMLQuotedTextImpl
import ps.shanty.intellij.mod.psi.ModFile

class ModElementGenerator(private val myProject: Project) {
    fun createYamlKeyValue(keyName: String, valueText: String): YAMLKeyValue {
        val tempValueFile: PsiFile = createDummyYamlWithText(valueText)
        val values = PsiTreeUtil.collectElementsOfType(
            tempValueFile,
            YAMLValue::class.java
        )
        val text: String
        text = if (!values.isEmpty() && values.iterator().next() is YAMLScalar && !valueText.contains("\n")) {
            "$keyName: $valueText"
        } else {
            """
     $keyName:
     ${YAMLTextUtil.indentText(valueText, 2)}
     """.trimIndent()
        }
        val tempFile: PsiFile = createDummyYamlWithText(text)
        return PsiTreeUtil.collectElementsOfType(tempFile, YAMLKeyValue::class.java).iterator().next()
    }

    fun createYamlDoubleQuotedString(): YAMLQuotedTextImpl {
        val tempFile = createDummyYamlWithText("\"foo\"")
        return PsiTreeUtil.collectElementsOfType(tempFile, YAMLQuotedTextImpl::class.java).iterator().next()
    }

    fun createDummyYamlWithText(text: String): YAMLFile {
        return PsiFileFactory.getInstance(myProject)
            .createFileFromText(
                "temp." + ModFileType.MOD.defaultExtension,
                ModFileType.MOD,
                text,
                LocalTimeCounter.currentTime(),
                true
            ) as ModFile
    }

    fun createEol(): PsiElement {
        val file = createDummyYamlWithText("\n")
        return PsiTreeUtil.getDeepestFirst(file)
    }

    fun createSpace(): PsiElement {
        val keyValue = createYamlKeyValue("foo", "bar")
        val whitespaceNode = keyValue.node.findChildByType(TokenType.WHITE_SPACE)!!
        return whitespaceNode.psi
    }

    fun createIndent(size: Int): PsiElement {
        val file = createDummyYamlWithText(StringUtil.repeatSymbol(' ', size))
        return PsiTreeUtil.getDeepestFirst(file)
    }

    fun createColon(): PsiElement {
        val file = createDummyYamlWithText("? foo : bar")
        val at = file.findElementAt("? foo ".length)
        assert(at != null && at.node.elementType === YAMLTokenTypes.COLON)
        return at!!
    }

    fun createDocumentMarker(): PsiElement {
        val file = createDummyYamlWithText("---")
        val at = file.findElementAt(0)
        assert(at != null && at.node.elementType === YAMLTokenTypes.DOCUMENT_MARKER)
        return at!!
    }

    fun createEmptySequence(): YAMLSequence {
        val sequence = PsiTreeUtil.findChildOfType(
            createDummyYamlWithText("- dummy"),
            YAMLSequence::class.java
        )!!
        sequence.deleteChildRange(sequence.firstChild, sequence.lastChild)
        return sequence
    }

    fun createEmptySequenceItem(): YAMLSequenceItem {
        val sequenceItem = PsiTreeUtil.findChildOfType(
            createDummyYamlWithText("- dummy"),
            YAMLSequenceItem::class.java
        )!!
        val value = sequenceItem.value!!
        value.deleteChildRange(value.firstChild, value.lastChild)
        return sequenceItem
    }

    companion object {
        fun getInstance(project: Project): ModElementGenerator {
            return project.getService(ModElementGenerator::class.java)
        }

        fun createChainedKey(keyComponents: List<String?>, indentAddition: Int): String {
            val sb = StringBuilder()
            for (i in keyComponents.indices) {
                if (i > 0) {
                    sb.append(StringUtil.repeatSymbol(' ', indentAddition + 2 * i))
                }
                sb.append(keyComponents[i]).append(":")
                if (i + 1 < keyComponents.size) {
                    sb.append('\n')
                }
            }
            return sb.toString()
        }
    }
}