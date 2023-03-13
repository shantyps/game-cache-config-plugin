package ps.shanty.intellij.mod.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl
import ps.shanty.intellij.mod.ModElementGenerator

class ModPlainTextManipulator : AbstractElementManipulator<ModPlainTextImpl>() {
    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(element: ModPlainTextImpl, range: TextRange, newContent: String): ModPlainTextImpl {
        return try {
            val encodeReplacements = element.getEncodeReplacements(newContent)
            val builder = StringBuilder()
            val oldText = element.text
            builder.append(oldText.subSequence(0, range.startOffset))
            builder.append(ModPlainTextImpl.processReplacements(newContent, encodeReplacements))
            builder.append(oldText.subSequence(range.endOffset, oldText.length))
            val dummyYamlFile =
                ModElementGenerator.getInstance(element.project).createDummyYamlWithText(builder.toString().removeFileExtension())
            val newScalar = PsiTreeUtil.collectElementsOfType(dummyYamlFile, ModPlainTextImpl::class.java).iterator().next()
            val result = element.replace(newScalar) as? ModPlainTextImpl
                ?: throw AssertionError("Inserted Mod scalar, but it isn't a scalar after insertion :(")

            // it is a hack to preserve the `QUICK_EDIT_HANDLERS` key,
            // actually `element.replace` should have done it, but for some reason didn't
            (element.node as UserDataHolderBase).copyCopyableDataTo((result.node as UserDataHolderBase))
            CodeEditUtil.setNodeGenerated(result.node, true)
            result
        } catch (e: IllegalArgumentException) {
            val newElement = element.replace(
                ModElementGenerator.getInstance(element.project).createYamlDoubleQuotedString()
            ) as? YAMLPlainTextImpl
                ?: throw AssertionError("Could not replace with dummy mod scalar")
            handleContentChange((newElement as ModPlainTextImpl), newContent)!!
        }
    }

    private fun String.removeFileExtension(): String {
        if (!contains('.')) return this
        return substring(0, lastIndexOf('.'))
    }
}