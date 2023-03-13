package ps.shanty.intellij.snt.psi

import com.intellij.lang.properties.psi.impl.PropertyImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException
import ps.shanty.intellij.snt.psi.impl.SNTEntryImpl

class SNTManipulator : AbstractElementManipulator<SNTEntryImpl>() {
    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(element: SNTEntryImpl, range: TextRange, newContent: String): SNTEntryImpl {
        val valueRange = getRangeInElement(element)
        val oldText = element.text
        val newText = oldText.substring(0, range.startOffset) + newContent + oldText.substring(range.endOffset)
        element.setValue(
            newText.substring(valueRange.startOffset).replace("([^\\s])\n".toRegex(), "$1 \n")
        ) // add explicit space before \n
        return element
    }

    override fun getRangeInElement(element: SNTEntryImpl): TextRange {
        val valueNode = element.valueNode ?: return TextRange.from(element.textLength, 0)
        val range = valueNode.textRange
        return TextRange.from(range.startOffset - element.textRange.startOffset, range.length)
    }
}