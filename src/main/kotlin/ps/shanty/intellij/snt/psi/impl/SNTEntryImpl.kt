package ps.shanty.intellij.snt.psi.impl

import com.intellij.lang.ASTFactory
import com.intellij.lang.ASTNode
import com.intellij.lang.properties.parsing.PropertiesTokenTypes
import com.intellij.lang.properties.psi.*
import com.intellij.lang.properties.psi.impl.PropertiesStubElementImpl
import com.intellij.lang.properties.psi.impl.PropertyImpl
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.IStubElementType
import com.intellij.util.IncorrectOperationException
import ps.shanty.intellij.PluginIcons
import ps.shanty.intellij.snt.psi.SNTEntry
import ps.shanty.intellij.snt.psi.SNTFile
import ps.shanty.intellij.snt.psi.SNTManipulator
import java.util.regex.Pattern
import javax.swing.Icon

class SNTEntryImpl : PropertiesStubElementImpl<PropertyStub?>, SNTEntry, PsiLanguageInjectionHost, PsiNameIdentifierOwner {
    constructor(node: ASTNode) : super(node)
    constructor(stub: PropertyStub?, nodeType: IStubElementType<*, *>?) : super(stub, nodeType)

    override fun toString(): String {
        return "Property{ key = $key, value = ${getValue()}}"
    }

    override fun getName(): String? {
        return unescapedKey
    }

    @Throws(IncorrectOperationException::class)
    override fun setName(name: String): PsiElement {
        val property = PropertiesElementFactory.createProperty(
            project, name, "xxx", null
        ) as PropertyImpl
        val keyNode = keyNode
        val newKeyNode = property.keyNode
        LOG.assertTrue(newKeyNode != null)
        if (keyNode == null) {
            node.addChild(newKeyNode!!)
        } else {
            node.replaceChild(keyNode, newKeyNode!!)
        }
        return this
    }

    @Throws(IncorrectOperationException::class)
    override fun setValue(value: String) {
        setValue(value, PropertyKeyValueFormat.PRESENTABLE)
    }

    @Throws(IncorrectOperationException::class)
    override fun setValue(value: String, format: PropertyKeyValueFormat) {
        val node = valueNode
        val property = PropertiesElementFactory.createProperty(
            project, "xxx", value,
            keyValueDelimiter, format
        ) as PropertyImpl
        val valueNode = property.valueNode
        if (node == null) {
            if (valueNode != null) {
                getNode().addChild(valueNode)
            }
        } else {
            if (valueNode == null) {
                getNode().removeChild(node)
            } else {
                getNode().replaceChild(node, valueNode)
            }
        }
    }

    override val key: String?
        get() {
            val stub = stub
            if (stub != null) {
                return stub.key
            }
            val node = keyNode ?: return null
            return node.text

        }

    val keyNode: ASTNode?
        get() = node.findChildByType(PropertiesTokenTypes.KEY_CHARACTERS)
    val valueNode: ASTNode?
        get() = node.findChildByType(PropertiesTokenTypes.VALUE_CHARACTERS)

    override fun getValue(): String {
        val node = valueNode ?: return ""
        return node.text
    }

    override val unescapedValue: String?
        get() = unescape(getValue())

    override fun getNameIdentifier(): PsiElement? {
        val node = keyNode
        return node?.psi
    }

    override val unescapedKey: String?
        get() = unescape(key)

    override fun getElementIcon(flags: Int): Icon {
        return PluginIcons.CHART
    }

    @Throws(IncorrectOperationException::class)
    override fun delete() {
        val parentNode = parent.node!!
        val node = node
        val prev = node.treePrev
        val next = node.treeNext
        parentNode.removeChild(node)
        if ((prev == null || prev.elementType === TokenType.WHITE_SPACE) && next != null && next.elementType === TokenType.WHITE_SPACE) {
            parentNode.removeChild(next)
        }
    }

    override val sntFile: SNTFile
        get() {
            val containingFile = super.getContainingFile()
            if (containingFile !is SNTFile) {
                LOG.error("Unexpected file type of: " + containingFile.name)
            }
            return containingFile as SNTFile
        }

    override val docCommentText: String?
        get() {
            val edge = getEdgeOfProperty(this)
            val text = StringBuilder()
            var doc = edge
            while (doc !== this) {
                if (doc is PsiComment) {
                    text.append(doc.getText())
                    text.append("\n")
                }
                doc = doc.nextSibling
            }
            return if (text.length == 0) null else text.toString()
        }

    override val psiElement: PsiElement
        get() = this

    override fun getUseScope(): SearchScope {
        // property ref can occur in any file
        return GlobalSearchScope.allScope(project)
    }

    override fun getPresentation(): ItemPresentation? {
        return object : ItemPresentation {
            override fun getPresentableText(): String? {
                return name
            }

            override fun getLocationString(): String? {
                return sntFile.getName()
            }

            override fun getIcon(open: Boolean): Icon? {
                return null
            }
        }
    }

    override fun isValidHost(): Boolean {
        return true
    }

    override fun updateText(text: String): PsiLanguageInjectionHost {
        return SNTManipulator().handleContentChange(this, text)!!
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return SNTEntryImplEscaper(this)
    }

    val keyValueDelimiter: Char
        get() {
            val delimiter =
                findChildByType<PsiElement>(PropertiesTokenTypes.KEY_VALUE_SEPARATOR) ?: return ' '
            val text = delimiter.text
            LOG.assertTrue(text.length == 1)
            return text[0]
        }

    fun replaceKeyValueDelimiterWithDefault() {
        val property = PropertiesElementFactory.createProperty(
            project, "yyy", "xxx", null
        ) as PropertyImpl
        val newDelimiter = property.node.findChildByType(PropertiesTokenTypes.KEY_VALUE_SEPARATOR)
        val propertyNode = node
        val oldDelimiter = propertyNode.findChildByType(PropertiesTokenTypes.KEY_VALUE_SEPARATOR)
        if (areDelimitersEqual(newDelimiter, oldDelimiter)) {
            return
        }
        if (newDelimiter == null) {
            propertyNode.replaceChild(oldDelimiter!!, ASTFactory.whitespace(" "))
        } else {
            if (oldDelimiter == null) {
                propertyNode.addChild(newDelimiter, valueNode)
                val insertedDelimiter = propertyNode.findChildByType(PropertiesTokenTypes.KEY_VALUE_SEPARATOR)
                LOG.assertTrue(insertedDelimiter != null)
                var currentPrev = insertedDelimiter!!.treePrev
                val toDelete: MutableList<ASTNode> = ArrayList()
                while (currentPrev != null && currentPrev.elementType === PropertiesTokenTypes.WHITE_SPACE) {
                    toDelete.add(currentPrev)
                    currentPrev = currentPrev.treePrev
                }
                for (node in toDelete) {
                    propertyNode.removeChild(node)
                }
            } else {
                propertyNode.replaceChild(oldDelimiter, newDelimiter)
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(
            PropertyImpl::class.java
        )
        private val PROPERTIES_SEPARATOR = Pattern.compile("^\\s*\\n\\s*\\n\\s*$")
        fun unescape(s: String?): String? {
            if (s == null) return null
            val sb = StringBuilder()
            parseCharacters(s, sb, null)
            return sb.toString()
        }

        fun parseCharacters(s: String, outChars: StringBuilder, sourceOffsets: IntArray?): Boolean {
            assert(sourceOffsets == null || sourceOffsets.size == s.length + 1)
            var off = 0
            val len = s.length
            var result = true
            val outOffset = outChars.length
            while (off < len) {
                var aChar = s[off++]
                if (sourceOffsets != null) {
                    sourceOffsets[outChars.length - outOffset] = off - 1
                    sourceOffsets[outChars.length + 1 - outOffset] = off
                }
                if (aChar == '\\') {
                    aChar = s[off++]
                    if (aChar == 'u') {
                        // Read the xxxx
                        var value = 0
                        var error = false
                        var i = 0
                        while (i < 4 && off < s.length) {
                            aChar = s[off++]
                            when (aChar) {
                                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> value =
                                    (value shl 4) + aChar.code - '0'.code

                                'a', 'b', 'c', 'd', 'e', 'f' -> value = (value shl 4) + 10 + aChar.code - 'a'.code
                                'A', 'B', 'C', 'D', 'E', 'F' -> value = (value shl 4) + 10 + aChar.code - 'A'.code
                                else -> {
                                    outChars.append("\\u")
                                    val start = off - i - 1
                                    val end = Math.min(start + 4, s.length)
                                    outChars.append(s, start, end)
                                    i = 4
                                    error = true
                                    off = end
                                }
                            }
                            i++
                        }
                        if (!error) {
                            outChars.append(value.toChar())
                        } else {
                            result = false
                        }
                    } else if (aChar == '\n') {
                        // escaped linebreak: skip whitespace in the beginning of next line
                        while (off < len && (s[off] == ' ' || s[off] == '\t')) {
                            off++
                        }
                    } else if (aChar == 't') {
                        outChars.append('\t')
                    } else if (aChar == 'r') {
                        outChars.append('\r')
                    } else if (aChar == 'n') {
                        outChars.append('\n')
                    } else if (aChar == 'f') {
                        outChars.append('\u000c')
                    } else {
                        outChars.append(aChar)
                    }
                } else {
                    outChars.append(aChar)
                }
                if (sourceOffsets != null) {
                    sourceOffsets[outChars.length - outOffset] = off
                }
            }
            return result
        }

        fun trailingSpaces(s: String?): TextRange? {
            if (s == null) {
                return null
            }
            var off = 0
            val len = s.length
            var startSpaces = -1
            while (off < len) {
                var aChar = s[off++]
                if (aChar == '\\') {
                    if (startSpaces == -1) startSpaces = off - 1
                    aChar = s[off++]
                    if (aChar == 'u') {
                        // Read the xxxx
                        var value = 0
                        var error = false
                        var i = 0
                        while (i < 4) {
                            aChar = if (off < s.length) s[off++] else 0.toChar()
                            when (aChar) {
                                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> value =
                                    (value shl 4) + aChar.code - '0'.code

                                'a', 'b', 'c', 'd', 'e', 'f' -> value = (value shl 4) + 10 + aChar.code - 'a'.code
                                'A', 'B', 'C', 'D', 'E', 'F' -> value = (value shl 4) + 10 + aChar.code - 'A'.code
                                else -> {
                                    val start = off - i - 1
                                    val end = Math.min(start + 4, s.length)
                                    i = 4
                                    error = true
                                    off = end
                                    startSpaces = -1
                                }
                            }
                            i++
                        }
                        if (!error) {
                            if (Character.isWhitespace(value)) {
                                if (startSpaces == -1) {
                                    startSpaces = off - 1
                                }
                            } else {
                                startSpaces = -1
                            }
                        }
                    } else if (aChar == '\n') {
                        // escaped linebreak: skip whitespace in the beginning of next line
                        while (off < len && (s[off] == ' ' || s[off] == '\t')) {
                            off++
                        }
                    } else if (aChar == 't' || aChar == 'r') {
                        if (startSpaces == -1) startSpaces = off
                    } else {
                        if (aChar == 'n' || aChar == 'f') {
                            if (startSpaces == -1) startSpaces = off
                        } else {
                            if (Character.isWhitespace(aChar)) {
                                if (startSpaces == -1) {
                                    startSpaces = off - 1
                                }
                            } else {
                                startSpaces = -1
                            }
                        }
                    }
                } else {
                    if (Character.isWhitespace(aChar)) {
                        if (startSpaces == -1) {
                            startSpaces = off - 1
                        }
                    } else {
                        startSpaces = -1
                    }
                }
            }
            return if (startSpaces == -1) null else TextRange(startSpaces, len)
        }

        fun getEdgeOfProperty(sntEntry: SNTEntry): PsiElement {
            var prev: PsiElement = sntEntry
            var node = sntEntry.prevSibling
            while (node != null) {
                if (node is Property) break
                if (node is PsiWhiteSpace) {
                    if (PROPERTIES_SEPARATOR.matcher(node.getText()).find()) break
                }
                prev = node
                node = node.prevSibling
            }
            return prev
        }

        private fun areDelimitersEqual(node1: ASTNode?, node2: ASTNode?): Boolean {
            if (node1 == null && node2 == null) return true
            if (node1 == null || node2 == null) return false
            val text1 = node1.text
            val text2 = node2.text
            return text1 == text2
        }
    }
}