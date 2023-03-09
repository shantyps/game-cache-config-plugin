package ps.shanty.intellij.parser

import com.intellij.lang.*
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.Stack
import org.jetbrains.yaml.YAMLBundle
import org.jetbrains.yaml.YAMLTokenTypes

class GameCacheConfigParser : PsiParser, LightPsiParser, YAMLTokenTypes {
    private var myBuilder: PsiBuilder? = null
    private var eolSeen = false
    private var myIndent = 0
    private var myAfterLastEolMarker: PsiBuilder.Marker? = null
    private val myStopTokensStack = Stack<TokenSet>()
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        parseLight(root, builder)
        return builder.treeBuilt
    }

    override fun parseLight(root: IElementType, builder: PsiBuilder) {
        myBuilder = builder
        myStopTokensStack.clear()
        val fileMarker = mark()
        parseFile()
        assert(myBuilder!!.eof()) { "Not all tokens were passed." }
        fileMarker.done(root)
    }

    private fun parseFile() {
        val marker = mark()
        passJunk()
        if (myBuilder!!.tokenType !== YAMLTokenTypes.DOCUMENT_MARKER) {
            dropEolMarker()
            marker.rollbackTo()
        } else {
            marker.drop()
        }
        do {
            parseDocument()
            passJunk()
        } while (!myBuilder!!.eof())
        dropEolMarker()
    }

    private fun parseDocument() {
        val marker = mark()
        if (myBuilder!!.tokenType === YAMLTokenTypes.DOCUMENT_MARKER) {
            advanceLexer()
        }
        parseBlockNode(myIndent, false)
        dropEolMarker()
        marker.done(GameCacheConfigElementTypes.DOCUMENT)
    }

    private fun parseBlockNode(indent: Int, insideSequence: Boolean) {
        // Preserve most test and current behaviour for most general cases without comments
        if (tokenType === YAMLTokenTypes.EOL) {
            advanceLexer()
            if (tokenType === YAMLTokenTypes.INDENT) {
                advanceLexer()
            }
        }
        val marker = mark()
        passJunk()
        var endOfNodeMarker: PsiBuilder.Marker? = null
        var nodeType: IElementType? = null


        // It looks like tag for a block node should be located on a separate line
        if (tokenType === YAMLTokenTypes.TAG && myBuilder!!.lookAhead(1) === YAMLTokenTypes.EOL) {
            advanceLexer()
        }
        var numberOfItems = 0
        while (!eof() && (isJunk || !eolSeen || myIndent + getIndentBonus(insideSequence) >= indent)) {
            if (isJunk) {
                advanceLexer()
                continue
            }
            if (!myStopTokensStack.isEmpty() && myStopTokensStack.peek().contains(tokenType)) {
                rollBackToEol()
                break
            }
            numberOfItems++
            val parsedTokenType = parseSingleStatement(if (eolSeen) myIndent else indent, indent)
            if (nodeType == null) {
                if (parsedTokenType === GameCacheConfigElementTypes.SEQUENCE_ITEM) {
                    nodeType = GameCacheConfigElementTypes.SEQUENCE
                } else if (parsedTokenType === GameCacheConfigElementTypes.KEY_VALUE_PAIR) {
                    nodeType = GameCacheConfigElementTypes.MAPPING
                } else if (numberOfItems > 1) {
                    nodeType = GameCacheConfigElementTypes.COMPOUND_VALUE
                }
            }
            endOfNodeMarker?.drop()
            endOfNodeMarker = mark()
        }
        if (endOfNodeMarker != null) {
            dropEolMarker()
            endOfNodeMarker.rollbackTo()
        } else {
            rollBackToEol()
        }
        includeBlockEmptyTail(indent)
        if (nodeType != null) {
            marker.done(nodeType)
            marker.setCustomEdgeTokenBinders(
                { tokens: List<IElementType>, atStreamEdge: Boolean, getter: WhitespacesAndCommentsBinder.TokenTextGetter? ->
                    findLeftRange(
                        tokens
                    )
                }
            ) { tokens: List<IElementType?>, atStreamEdge: Boolean, getter: WhitespacesAndCommentsBinder.TokenTextGetter? -> tokens.size }
        } else {
            marker.drop()
        }
    }

    private fun includeBlockEmptyTail(indent: Int) {
        if (indent == 0) {
            // top-level block with zero indent
            while (isJunk) {
                if (tokenType === YAMLTokenTypes.EOL) {
                    if (!GameCacheConfigElementTypes.BLANK_ELEMENTS.contains(myBuilder!!.lookAhead(1))) {
                        // do not include last \n into block
                        break
                    }
                }
                advanceLexer()
                dropEolMarker()
            }
        } else {
            var endOfBlock = mark()
            while (isJunk) {
                if (tokenType === YAMLTokenTypes.INDENT && currentTokenLength >= indent) {
                    dropEolMarker()
                    endOfBlock.drop()
                    advanceLexer()
                    endOfBlock = mark()
                } else {
                    advanceLexer()
                    dropEolMarker()
                }
            }
            endOfBlock.rollbackTo()
        }
    }

    /**
     * @link {http://www.yaml.org/spec/1.2/spec.html#id2777534}
     */
    private fun getIndentBonus(insideSequence: Boolean): Int {
        return if (!insideSequence && tokenType === YAMLTokenTypes.SEQUENCE_MARKER) {
            1
        } else {
            0
        }
    }

    private val shorthandIndentAddition: Int
        private get() {
            val offset = myBuilder!!.currentOffset
            val nextToken = myBuilder!!.lookAhead(1)
            if (nextToken !== YAMLTokenTypes.SEQUENCE_MARKER && nextToken !== YAMLTokenTypes.SCALAR_KEY) {
                return 1
            }
            return if (myBuilder!!.rawLookup(1) === YAMLTokenTypes.WHITESPACE) {
                myBuilder!!.rawTokenTypeStart(2) - offset
            } else {
                1
            }
        }

    private fun parseSingleStatement(indent: Int, minIndent: Int): IElementType? {
        if (eof()) {
            return null
        }
        val marker = mark()
        parseNodeProperties()
        val tokenType = tokenType
        val nodeType: IElementType?
        if (tokenType === YAMLTokenTypes.LBRACE) {
            nodeType = parseHash()
        } else if (tokenType === YAMLTokenTypes.LBRACKET) {
            nodeType = parseArray()
        } else if (tokenType === YAMLTokenTypes.SEQUENCE_MARKER) {
            nodeType = parseSequenceItem(indent)
        } else if (tokenType === YAMLTokenTypes.QUESTION) {
            nodeType = parseExplicitKeyValue(indent)
        } else if (tokenType === YAMLTokenTypes.SCALAR_KEY) {
            nodeType = parseScalarKeyValue(indent)
        } else if (GameCacheConfigElementTypes.SCALAR_VALUES.contains(this.tokenType)) {
            nodeType = parseScalarValue(minIndent)
        } else if (tokenType === YAMLTokenTypes.STAR) {
            val aliasMarker = mark()
            advanceLexer() // symbol *
            if (this.tokenType === YAMLTokenTypes.ALIAS) {
                advanceLexer() // alias name
                aliasMarker.done(GameCacheConfigElementTypes.ALIAS_NODE)
                if (this.tokenType === YAMLTokenTypes.COLON) {
                    // Alias is used as key name
                    eolSeen = false
                    val indentAddition = shorthandIndentAddition
                    nodeType = parseSimpleScalarKeyValueFromColon(indent, indentAddition)
                } else {
                    // simple ALIAS_NODE was constructed and marker should be dropped
                    marker.drop()
                    return GameCacheConfigElementTypes.ALIAS_NODE
                }
            } else {
                // Should be impossible now (because of lexer rules)
                aliasMarker.drop()
                nodeType = null
            }
        } else {
            advanceLexer()
            nodeType = null
        }
        if (nodeType != null) {
            marker.done(nodeType)
        } else {
            marker.drop()
        }
        return nodeType
    }

    /**
     * Each node may have two optional properties, anchor and tag, in addition to its content.
     * Node properties may be specified in any order before the node’s content.
     * Either or both may be omitted.
     *
     * <pre>
     * [96] c-ns-properties(n,c) ::= ( c-ns-tag-property ( s-separate(n,c) c-ns-anchor-property )? )
     * | ( c-ns-anchor-property ( s-separate(n,c) c-ns-tag-property )? )
     *
    </pre> *
     * See [6.9. Node Properties](http://www.yaml.org/spec/1.2/spec.html#id2783797)
     */
    private fun parseNodeProperties() {
        // By standard here could be no more than one TAG or ANCHOR
        // By better to support sequence of them
        var anchorWasRead = false
        var tagWasRead = false
        while (tokenType === YAMLTokenTypes.TAG || tokenType === YAMLTokenTypes.AMPERSAND) {
            if (tokenType === YAMLTokenTypes.AMPERSAND) {
                var errorMarker: PsiBuilder.Marker? = null
                if (anchorWasRead) {
                    errorMarker = mark()
                }
                anchorWasRead = true
                val anchorMarker = mark()
                advanceLexer() // symbol &
                if (tokenType === YAMLTokenTypes.ANCHOR) {
                    advanceLexer() // anchor name
                    anchorMarker.done(GameCacheConfigElementTypes.ANCHOR_NODE)
                } else {
                    // Should be impossible now (because of lexer rules)
                    anchorMarker.drop()
                }
                errorMarker?.error(YAMLBundle.message("YAMLParser.multiple.anchors"))
            } else { // tag case
                if (tagWasRead) {
                    val errorMarker = mark()
                    advanceLexer()
                    errorMarker.error(YAMLBundle.message("YAMLParser.multiple.tags"))
                } else {
                    tagWasRead = true
                    advanceLexer()
                }
            }
        }
    }

    private fun parseScalarValue(indent: Int): IElementType? {
        val tokenType = tokenType
        assert(GameCacheConfigElementTypes.SCALAR_VALUES.contains(tokenType)) { "Scalar value expected!" }
        return if (tokenType === YAMLTokenTypes.SCALAR_LIST || tokenType === YAMLTokenTypes.SCALAR_TEXT) {
            parseMultiLineScalar(tokenType)
        } else if (tokenType === YAMLTokenTypes.TEXT) {
            parseMultiLinePlainScalar(indent)
        } else if (tokenType === YAMLTokenTypes.SCALAR_DSTRING || tokenType === YAMLTokenTypes.SCALAR_STRING) {
            parseQuotedString()
        } else {
            advanceLexer()
            null
        }
    }

    private fun parseQuotedString(): IElementType {
        advanceLexer()
        return GameCacheConfigElementTypes.SCALAR_QUOTED_STRING
    }

    private fun parseMultiLineScalar(tokenType: IElementType?): IElementType {
        assert(tokenType === this.tokenType)
        // Accept header token: '|' or '>'
        advanceLexer()

        // Parse header tail: TEXT is used as placeholder for invalid symbols in this context
        if (this.tokenType === YAMLTokenTypes.TEXT) {
            val err = myBuilder!!.mark()
            advanceLexer()
            err.error(YAMLBundle.message("YAMLParser.invalid.header.symbols"))
        }
        if (GameCacheConfigElementTypes.EOL_ELEMENTS.contains(this.tokenType)) {
            advanceLexer()
        }
        var endOfValue: PsiBuilder.Marker? = myBuilder!!.mark()
        var type = this.tokenType
        // Lexer ensures such input token structure: ( ( INDENT tokenType? )? SCALAR_EOL )*
        // endOfValue marker is needed to exclude INDENT after last SCALAR_EOL
        while (type === tokenType || type === YAMLTokenTypes.INDENT || type === YAMLTokenTypes.SCALAR_EOL) {
            advanceLexer()
            if (type === tokenType) {
                endOfValue?.drop()
                endOfValue = null
            }
            if (type === YAMLTokenTypes.SCALAR_EOL) {
                endOfValue?.drop()
                endOfValue = myBuilder!!.mark()
            }
            type = this.tokenType
        }
        endOfValue?.rollbackTo()
        return if (tokenType === YAMLTokenTypes.SCALAR_LIST) GameCacheConfigElementTypes.SCALAR_LIST_VALUE else GameCacheConfigElementTypes.SCALAR_TEXT_VALUE
    }

    private fun parseMultiLinePlainScalar(indent: Int): IElementType {
        var lastTextEnd: PsiBuilder.Marker? = null
        var type = tokenType
        while (type === YAMLTokenTypes.TEXT || type === YAMLTokenTypes.INDENT || type === YAMLTokenTypes.EOL) {
            advanceLexer()
            if (type === YAMLTokenTypes.TEXT) {
                if (lastTextEnd != null && myIndent < indent) {
                    break
                }
                lastTextEnd?.drop()
                lastTextEnd = mark()
            }
            type = tokenType
        }
        rollBackToEol()
        assert(lastTextEnd != null)
        lastTextEnd!!.rollbackTo()
        return GameCacheConfigElementTypes.SCALAR_PLAIN_VALUE
    }

    private fun parseExplicitKeyValue(indent: Int): IElementType {
        assert(tokenType === YAMLTokenTypes.QUESTION)
        var indentAddition = shorthandIndentAddition
        advanceLexer()
        if (// This means we're inside some hash
            !myStopTokensStack.isEmpty() && myStopTokensStack.peek() == HASH_STOP_TOKENS && tokenType === YAMLTokenTypes.SCALAR_KEY) {
            parseScalarKeyValue(indent)
        } else {
            myStopTokensStack.add(TokenSet.create(YAMLTokenTypes.COLON))
            eolSeen = false
            parseBlockNode(indent + indentAddition, false)
            myStopTokensStack.pop()
            passJunk()
            if (tokenType === YAMLTokenTypes.COLON) {
                indentAddition = shorthandIndentAddition
                advanceLexer()
                eolSeen = false
                parseBlockNode(indent + indentAddition, false)
            }
        }
        return GameCacheConfigElementTypes.KEY_VALUE_PAIR
    }

    private fun parseScalarKeyValue(indent: Int): IElementType {
        assert(tokenType === YAMLTokenTypes.SCALAR_KEY) { "Expected scalar key" }
        eolSeen = false
        val indentAddition = shorthandIndentAddition
        advanceLexer()
        return parseSimpleScalarKeyValueFromColon(indent, indentAddition)
    }

    private fun parseSimpleScalarKeyValueFromColon(indent: Int, indentAddition: Int): IElementType {
        assert(tokenType === YAMLTokenTypes.COLON) { "Expected colon" }
        advanceLexer()
        val rollbackMarker = mark()
        passJunk()
        if (eolSeen && (eof() || myIndent + getIndentBonus(false) < indent + indentAddition)) {
            dropEolMarker()
            rollbackMarker.rollbackTo()
        } else {
            dropEolMarker()
            rollbackMarker.rollbackTo()
            parseBlockNode(indent + indentAddition, false)
        }
        return GameCacheConfigElementTypes.KEY_VALUE_PAIR
    }

    private fun parseSequenceItem(indent: Int): IElementType {
        assert(tokenType === YAMLTokenTypes.SEQUENCE_MARKER)
        val indentAddition = shorthandIndentAddition
        advanceLexer()
        eolSeen = false
        parseBlockNode(indent + indentAddition, true)
        rollBackToEol()
        return GameCacheConfigElementTypes.SEQUENCE_ITEM
    }

    private fun parseHash(): IElementType {
        assert(tokenType === YAMLTokenTypes.LBRACE)
        advanceLexer()
        myStopTokensStack.add(HASH_STOP_TOKENS)
        while (!eof()) {
            if (tokenType === YAMLTokenTypes.RBRACE) {
                advanceLexer()
                break
            }
            parseSingleStatement(0, 0)
        }
        myStopTokensStack.pop()
        dropEolMarker()
        return GameCacheConfigElementTypes.HASH
    }

    private fun parseArray(): IElementType {
        assert(tokenType === YAMLTokenTypes.LBRACKET)
        advanceLexer()
        myStopTokensStack.add(ARRAY_STOP_TOKENS)
        while (!eof()) {
            if (tokenType === YAMLTokenTypes.RBRACKET) {
                advanceLexer()
                break
            }
            if (isJunk) {
                advanceLexer()
                continue
            }
            val marker = mark()
            val parsedElement = parseSingleStatement(0, 0)
            if (parsedElement != null) {
                marker.done(GameCacheConfigElementTypes.SEQUENCE_ITEM)
            } else {
                marker.error(YAMLBundle.message("parsing.error.sequence.item.expected"))
            }
            if (tokenType === YAMLTokenTypes.COMMA) {
                advanceLexer()
            }
        }
        myStopTokensStack.pop()
        dropEolMarker()
        return GameCacheConfigElementTypes.ARRAY
    }

    private fun eof(): Boolean {
        return myBuilder!!.eof() || myBuilder!!.tokenType === YAMLTokenTypes.DOCUMENT_MARKER
    }

    private val tokenType: IElementType?
        private get() = if (eof()) null else myBuilder!!.tokenType

    private fun dropEolMarker() {
        if (myAfterLastEolMarker != null) {
            myAfterLastEolMarker!!.drop()
            myAfterLastEolMarker = null
        }
    }

    private fun rollBackToEol() {
        if (eolSeen && myAfterLastEolMarker != null) {
            eolSeen = false
            myAfterLastEolMarker!!.rollbackTo()
            myAfterLastEolMarker = null
        }
    }

    private fun mark(): PsiBuilder.Marker {
        dropEolMarker()
        return myBuilder!!.mark()
    }

    private fun advanceLexer() {
        if (myBuilder!!.eof()) {
            return
        }
        val type = myBuilder!!.tokenType
        val eolElement = GameCacheConfigElementTypes.EOL_ELEMENTS.contains(type)
        eolSeen = eolSeen || eolElement
        if (eolElement) {
            // Drop and create new eolMarker
            myAfterLastEolMarker = mark()
            myIndent = 0
        } else if (type === YAMLTokenTypes.INDENT) {
            myIndent = currentTokenLength
        } else {
            // Drop Eol Marker if other token seen
            dropEolMarker()
        }
        myBuilder!!.advanceLexer()
    }

    private val currentTokenLength: Int
        private get() = myBuilder!!.rawTokenTypeStart(1) - myBuilder!!.currentOffset

    private fun passJunk() {
        while (!eof() && isJunk) {
            advanceLexer()
        }
    }

    private val isJunk: Boolean
        private get() {
            val type = tokenType
            return type === YAMLTokenTypes.INDENT || type === YAMLTokenTypes.EOL
        }

    companion object {
        val HASH_STOP_TOKENS = TokenSet.create(YAMLTokenTypes.RBRACE, YAMLTokenTypes.COMMA)
        val ARRAY_STOP_TOKENS = TokenSet.create(YAMLTokenTypes.RBRACKET, YAMLTokenTypes.COMMA)
        private fun findLeftRange(tokens: List<IElementType>): Int {
            val i = tokens.indexOf(YAMLTokenTypes.COMMENT)
            return if (i != -1) i else tokens.size
        }
    }
}