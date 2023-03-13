package ps.shanty.intellij.snt.psi.impl

import com.intellij.lang.properties.psi.impl.PropertyImpl
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper

internal class SNTEntryImplEscaper(value: SNTEntryImpl) : LiteralTextEscaper<SNTEntryImpl>(value) {
    private lateinit var outSourceOffsets: IntArray
    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        val subText = rangeInsideHost.substring(myHost!!.text)
        outSourceOffsets = IntArray(subText.length + 1)
        val prefixLen = outChars.length
        val b = PropertyImpl.parseCharacters(subText, outChars, outSourceOffsets)
        if (b) {
            var i = prefixLen
            val len = outChars.length
            while (i < len) {
                val outChar = outChars[i]
                val inChar = subText[outSourceOffsets[i - prefixLen]]
                if (outChar != inChar && inChar != '\\') {
                    LOG.error(
                        """
                            input: $subText;
                            output: $outChars
                            at: $i; prefix-length: $prefixLen
                            """.trimIndent()
                    )
                }
                i++
            }
        }
        return b
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        val result = if (offsetInDecoded < outSourceOffsets.size) outSourceOffsets[offsetInDecoded] else -1
        return if (result == -1) -1 else Math.min(result, rangeInsideHost.length) + rangeInsideHost.startOffset
    }

    override fun isOneLine(): Boolean {
        return !myHost!!.text.contains("\\")
    }

    companion object {
        private val LOG = Logger.getInstance(SNTEntryImpl::class.java)
    }
}