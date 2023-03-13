package ps.shanty.intellij.mod.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl

class ModPlainTextImpl(node: ASTNode) : YAMLPlainTextImpl(node) {
    public override fun getEncodeReplacements(input: CharSequence): List<Pair<TextRange, String>> {
        return super.getEncodeReplacements(input)
    }

    override fun toString(): String {
        return "Mod plain scalar text"
    }

    companion object {
        @Throws(IndexOutOfBoundsException::class)
        fun processReplacements(
            input: CharSequence,
            replacements: List<Pair<TextRange, String>>
        ): String {
            val result = StringBuilder()
            var currentOffset = 0
            for (replacement in replacements) {
                result.append(input.subSequence(currentOffset, replacement.getFirst().startOffset))
                result.append(replacement.getSecond())
                currentOffset = replacement.getFirst().endOffset
            }
            result.append(input.subSequence(currentOffset, input.length))
            return result.toString()
        }
    }
}