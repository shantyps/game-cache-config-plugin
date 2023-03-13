package ps.shanty.intellij.mod.smart

import com.intellij.codeInsight.editorActions.QuoteHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import ps.shanty.intellij.mod.ModElementTypes

class ModQuoteHandler : QuoteHandler {

    private fun isQuote(it: Char?) = it == '"' || it == '\''

    private fun isOneQuote(iterator: HighlighterIterator): Boolean {
        with(iterator) {
            if (!ModElementTypes.TEXT_SCALAR_ITEMS.contains(tokenType)) return false
            return isQuote(document.charsSequence[start]) && !isQuote(document.charsSequence.getOrNull(start + 1))
        }
    }

    override fun isClosingQuote(iterator: HighlighterIterator, offset: Int): Boolean = isOneQuote(iterator) && iterator.end == offset

    override fun isOpeningQuote(iterator: HighlighterIterator, offset: Int): Boolean =
        isOneQuote(iterator) && with(iterator) { start == offset || end - start == 1 }

    override fun hasNonClosedLiteral(editor: Editor, iterator: HighlighterIterator, offset: Int): Boolean = isOneQuote(iterator)

    override fun isInsideLiteral(iterator: HighlighterIterator): Boolean = false

    companion object {
        fun removeQuotes(key: String): String {
            if (key.startsWith("\"") && key.endsWith("\"")) {
                return key.substring(1, key.length - 1)
            }
            return key
        }
    }
}