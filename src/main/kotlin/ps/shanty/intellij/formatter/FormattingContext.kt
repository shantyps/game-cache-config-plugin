package ps.shanty.intellij.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.templateLanguages.OuterLanguageElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.ObjectUtils
import com.intellij.util.containers.FactoryMap
import one.util.streamex.StreamEx
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLSequenceItem
import org.jetbrains.yaml.psi.YAMLValue
import ps.shanty.intellij.parser.GameCacheConfigElementTypes
import ps.shanty.intellij.parser.GameCacheConfigFileType
import ps.shanty.intellij.parser.GameCacheConfigLanguage
import ps.shanty.intellij.parser.GameCacheConfigKeyValue
import java.util.*
import java.util.function.Predicate

class FormattingContext(val mySettings: CodeStyleSettings, private val myFile: PsiFile) {
    private val mySpaceBuilder: SpacingBuilder

    /** This alignments increase partial reformatting stability in case of initially incorrect indents  */
    private val myChildIndentAlignments = FactoryMap.create { node: ASTNode? ->
        Alignment.createAlignment(
            true
        )
    }
    private val myChildValueAlignments = FactoryMap.create { node: ASTNode? ->
        Alignment.createAlignment(
            true
        )
    }
    private val shouldIndentSequenceValue: Boolean
    private val shouldInlineSequenceIntoSequence: Boolean
    private val shouldInlineBlockMappingIntoSequence: Boolean
    private val getValueAlignment: Int
    private var myFullText: String? = null

    init {
        val custom = mySettings.getCustomSettings(CustomCodeStyleSettings::class.java)
        val common = mySettings.getCommonSettings(GameCacheConfigLanguage.INSTANCE)
        mySpaceBuilder = SpacingBuilder(mySettings, GameCacheConfigLanguage.INSTANCE)
            .between(YAMLTokenTypes.COLON, GameCacheConfigElementTypes.KEY_VALUE_PAIR).lineBreakInCode()
            .between(YAMLTokenTypes.COLON, GameCacheConfigElementTypes.SEQUENCE_ITEM).lineBreakInCode()
            .between(GameCacheConfigElementTypes.ALIAS_NODE, YAMLTokenTypes.COLON).spaces(1)
            .before(YAMLTokenTypes.COLON).spaceIf(custom.SPACE_BEFORE_COLON)
            .after(YAMLTokenTypes.COLON).spaces(1)
            .after(YAMLTokenTypes.LBRACKET).spaceIf(common.SPACE_WITHIN_BRACKETS)
            .before(YAMLTokenTypes.RBRACKET).spaceIf(common.SPACE_WITHIN_BRACKETS)
            .after(YAMLTokenTypes.LBRACE).spaceIf(common.SPACE_WITHIN_BRACES)
            .before(YAMLTokenTypes.RBRACE).spaceIf(common.SPACE_WITHIN_BRACES)
        shouldIndentSequenceValue = custom.INDENT_SEQUENCE_VALUE
        shouldInlineSequenceIntoSequence = !custom.SEQUENCE_ON_NEW_LINE
        shouldInlineBlockMappingIntoSequence = !custom.BLOCK_MAPPING_ON_NEW_LINE
        getValueAlignment = custom.ALIGN_VALUES_PROPERTIES
    }

    fun computeSpacing(parent: Block, child1: Block?, child2: Block): Spacing? {
        if (child1 is ASTBlock && endsWithTemplate(
                child1.node
            )
        ) {
            return null
        }
        if (child2 is ASTBlock && startsWithTemplate(
                child2.node
            )
        ) {
            val astNode = child2.node
            if (!isAdjectiveToMinus(astNode)) {
                if (isAfterKey(astNode)) {
                    return mySpaceBuilder.getSpacing(
                        parent,
                        getNodeElementType(parent),
                        YAMLTokenTypes.COLON,
                        YAMLTokenTypes.SCALAR_TEXT
                    )
                }
                if (isAfterSequenceMarker(astNode)) {
                    return getSpacingAfterSequenceMarker(child1, child2)
                }
            }
            return null
        }
        val simpleSpacing = mySpaceBuilder.getSpacing(parent, child1, child2)
        return simpleSpacing ?: getSpacingAfterSequenceMarker(child1, child2)
    }

    private fun getSpacingAfterSequenceMarker(child1: Block?, child2: Block): Spacing? {
        if (!(child1 is ASTBlock && child2 is ASTBlock)) {
            return null
        }
        val node1 = child1.node
        val node2 = child2.node
        if (PsiUtilCore.getElementType(node1) !== YAMLTokenTypes.SEQUENCE_MARKER) {
            return null
        }
        val node2Type = PsiUtilCore.getElementType(node2)
        var indentSize = mySettings.getIndentSize(GameCacheConfigFileType.GAME_CACHE_CONFIG)
        if (indentSize < 2) {
            indentSize = 2
        }
        var spaces = 1
        var minLineFeeds = 0
        if (node2Type === GameCacheConfigElementTypes.SEQUENCE_ITEM) {
            if (shouldInlineSequenceIntoSequence) {
                // Set spaces to fit other items indent:
                // -   - a # 3 spaces here if indent size is 4
                //     - b
                spaces = indentSize - 1
            } else {
                minLineFeeds = 1
            }
        } else if (node2Type === GameCacheConfigElementTypes.KEY_VALUE_PAIR) {
            if (shouldInlineBlockMappingIntoSequence) {
                // Set spaces to fit other items indent:
                // -   a: x # 3 spaces here if indent size is 4
                //     b: y
                spaces = indentSize - 1
            } else {
                minLineFeeds = 1
            }
        }
        return Spacing.createSpacing(spaces, spaces, minLineFeeds, false, 0)
    }

    fun computeAlignment(node: ASTNode): Alignment? {
        val type = PsiUtilCore.getElementType(node)
        if (type === GameCacheConfigElementTypes.SEQUENCE_ITEM) {
            if (node.treeParent.elementType === GameCacheConfigElementTypes.ARRAY) {
                val sequence = node.treeParent.psi as YAMLSequence
                for (child in sequence.items) {
                    // do not align multiline elements in json-style arrays
                    if (child.textContains('\n')) {
                        return null
                    }
                }
            }
            // Anyway we need to align `-` symbols in block-style sequences
            return myChildIndentAlignments[node.treeParent]
        }
        if (type === GameCacheConfigElementTypes.KEY_VALUE_PAIR) {
            return myChildIndentAlignments[node.treeParent]
        }
        if (getValueAlignment == CustomCodeStyleSettings.ALIGN_ON_COLON) {
            if (type === YAMLTokenTypes.COLON) {
                return myChildValueAlignments[node.treeParent.treeParent]
            }
        } else if (getValueAlignment == CustomCodeStyleSettings.ALIGN_ON_VALUE) {
            if (GameCacheConfigElementTypes.SCALAR_ITEMS.contains(type)) {
                // for block scalar here we consider only headers
                val prev = getPreviousNonBlankNode(node.treeParent)
                if (PsiUtilCore.getElementType(prev) === YAMLTokenTypes.COLON) {
                    return myChildValueAlignments[prev!!.treeParent.treeParent]
                }
            }
        }
        return null
    }

    fun computeBlockIndent(node: ASTNode): Indent? {
        if (node is OuterLanguageElement) {
            val templateIndent = computeTemplateIndent((node as OuterLanguageElement).textRange)
            if (templateIndent != null) return templateIndent
        }
        val nodeType = PsiUtilCore.getElementType(node)
        val parentType = PsiUtilCore.getElementType(node.treeParent)
        val grandParentType = if (parentType == null) null else PsiUtilCore.getElementType(node.treeParent.treeParent)
        val grand2ParentType =
            if (grandParentType == null) null else PsiUtilCore.getElementType(node.treeParent.treeParent.treeParent)
        assert(nodeType !== GameCacheConfigElementTypes.SEQUENCE) { "Sequence should be inlined!" }
        assert(nodeType !== GameCacheConfigElementTypes.MAPPING) { "Mapping should be inlined!" }
        assert(nodeType !== GameCacheConfigElementTypes.DOCUMENT) { "Document should be inlined!" }
        return if (GameCacheConfigElementTypes.DOCUMENT_BRACKETS.contains(nodeType)) {
            SAME_AS_PARENT_INDENT
        } else if (GameCacheConfigElementTypes.BRACKETS.contains(nodeType)) {
            SAME_AS_INDENTED_ANCESTOR_INDENT
        } else if (GameCacheConfigElementTypes.TEXT_SCALAR_ITEMS.contains(nodeType)) {
            if (grandParentType === GameCacheConfigElementTypes.DOCUMENT) {
                return SAME_AS_PARENT_INDENT
            }
            if (grand2ParentType === GameCacheConfigElementTypes.ARRAY || grand2ParentType === GameCacheConfigElementTypes.HASH) {
                Indent.getContinuationWithoutFirstIndent()
            } else DIRECT_NORMAL_INDENT
        } else if (nodeType === GameCacheConfigElementTypes.FILE) {
            SAME_AS_PARENT_INDENT
        } else if (GameCacheConfigElementTypes.SCALAR_VALUES.contains(nodeType)) {
            DIRECT_NORMAL_INDENT
        } else if (nodeType === GameCacheConfigElementTypes.SEQUENCE_ITEM) {
            computeSequenceItemIndent(node)
        } else if (nodeType === GameCacheConfigElementTypes.KEY_VALUE_PAIR) {
            computeKeyValuePairIndent(node)
        } else {
            if (nodeType === YAMLTokenTypes.COMMENT) {
                if (parentType === GameCacheConfigElementTypes.SEQUENCE) {
                    return computeSequenceItemIndent(node)
                }
                if (parentType === GameCacheConfigElementTypes.MAPPING) {
                    return computeKeyValuePairIndent(node)
                }
            }
            if (GameCacheConfigElementTypes.TOP_LEVEL.contains(parentType)) SAME_AS_PARENT_INDENT else null
        }
    }

    private fun computeTemplateIndent(nodeTextRange: TextRange): Indent? {
        val document = PsiDocumentManager.getInstance(myFile.project).getDocument(myFile) ?: return null
        val lineNumber = document.getLineNumber(nodeTextRange.startOffset)
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        return if (!document.charsSequence.subSequence(lineStartOffset, nodeTextRange.startOffset)
                .isBlank()
        ) null else IndentImpl(
            Indent.Type.SPACES, true, nodeTextRange.startOffset - lineStartOffset, false, false
        )
    }

    fun computeNewChildIndent(node: ASTNode): Indent? {
        return if (GameCacheConfigElementTypes.TOP_LEVEL.contains(PsiUtilCore.getElementType(node))) SAME_AS_PARENT_INDENT else DIRECT_NORMAL_INDENT
    }

    fun isIncomplete(node: ASTNode): Boolean {
        val possiblyIncompleteValue =
            Predicate { value: YAMLValue? ->
                value == null || GameCacheConfigElementTypes.INCOMPLETE_BLOCKS.contains(
                    PsiUtilCore.getElementType(value)
                )
            }
        if (PsiUtilCore.getElementType(node) === GameCacheConfigElementTypes.KEY_VALUE_PAIR) {
            val value = (node.psi as GameCacheConfigKeyValue).value
            if (possiblyIncompleteValue.test(value)) {
                return true
            }
        } else if (PsiUtilCore.getElementType(node) === GameCacheConfigElementTypes.SEQUENCE_ITEM) {
            val value = (node.psi as YAMLSequenceItem).value
            if (possiblyIncompleteValue.test(value)) {
                return true
            }
        }
        return FormatterUtil.isIncomplete(node)
    }

    val fullText: String
        get() {
            if (myFullText == null) {
                myFullText = myFile.text
            }
            return myFullText!!
        }

    private fun computeSequenceItemIndent(node: ASTNode): Indent {
        val parentType = PsiUtilCore.getElementType(node.treeParent)
        val grandParentType = if (parentType == null) null else PsiUtilCore.getElementType(node.treeParent.treeParent)
        val grandParentIsDocument = grandParentType === GameCacheConfigElementTypes.DOCUMENT
        return if (parentType === GameCacheConfigElementTypes.ARRAY) {
            Indent.getNormalIndent()
        } else if (grandParentType === GameCacheConfigElementTypes.KEY_VALUE_PAIR) {
            if (shouldIndentSequenceValue) {
                // key:
                //   - x
                //   - y
                DIRECT_NORMAL_INDENT
            } else {
                // key:
                // - x
                // - y
                SAME_AS_PARENT_INDENT
            }
        } else if (grandParentIsDocument) {
            SAME_AS_PARENT_INDENT
        } else {
            // - - x
            //   - y
            // or
            // -
            //   - x
            //   - y
            DIRECT_NORMAL_INDENT
        }
    }

    companion object {
        private val DIRECT_NORMAL_INDENT = Indent.getNormalIndent(true)
        private val SAME_AS_PARENT_INDENT = Indent.getSpaceIndent(0, true)
        private val SAME_AS_INDENTED_ANCESTOR_INDENT = Indent.getSpaceIndent(0)
        private val NON_SIGNIFICANT_TOKENS_BEFORE_TEMPLATE =
            TokenSet.create(TokenType.WHITE_SPACE, YAMLTokenTypes.SEQUENCE_MARKER)

        private fun isAfterKey(node: ASTNode?): Boolean {
            val nodes = StreamEx.iterate(node,
                { obj: ASTNode? ->
                    Objects.nonNull(
                        obj
                    )
                }
            ) { node: ASTNode? ->
                TreeUtil.prevLeaf(
                    node
                )
            }.skip(1)
                .dropWhile { n: ASTNode? ->
                    NON_SIGNIFICANT_TOKENS_BEFORE_TEMPLATE.contains(
                        n!!.elementType
                    )
                }.limit(2).toList()
            return if (nodes.size != 2) false else YAMLTokenTypes.COLON == nodes[0]!!.elementType && YAMLTokenTypes.SCALAR_KEY == nodes[1]!!
                .elementType
        }

        private fun isAfterSequenceMarker(node: ASTNode?): Boolean {
            val nodes = StreamEx.iterate(node,
                { obj: ASTNode? ->
                    Objects.nonNull(
                        obj
                    )
                }
            ) { n: ASTNode? -> n!!.treePrev }.skip(1)
                .filter { n: ASTNode? ->
                    !GameCacheConfigElementTypes.SPACE_ELEMENTS.contains(
                        n!!.elementType
                    )
                }
                .takeWhile { n: ASTNode? -> YAMLTokenTypes.EOL != n!!.elementType }.limit(2).toList()
            return if (nodes.size != 1) false else YAMLTokenTypes.SEQUENCE_MARKER == nodes[0]!!.elementType
        }

        private fun isAdjectiveToMinus(node: ASTNode?): Boolean {
            val prevLeaf = TreeUtil.prevLeaf(node)
            // we don't consider`-` before template as a seq marker if there is no space before it, because it could be a `-1` value for instance
            return prevLeaf != null && YAMLTokenTypes.SEQUENCE_MARKER == prevLeaf.elementType
        }

        private fun getNodeElementType(parent: Block?): IElementType? {
            if (parent == null) return null
            val it = ObjectUtils.tryCast(
                parent,
                ASTBlock::class.java
            ) ?: return null
            val node = it.node ?: return null
            return node.elementType
        }

        private fun startsWithTemplate(astNode: ASTNode?): Boolean {
            var astNode = astNode
            while (astNode != null) {
                if (astNode is OuterLanguageElement) return true
                astNode = if (NON_SIGNIFICANT_TOKENS_BEFORE_TEMPLATE.contains(astNode.elementType)) {
                    astNode.treeNext
                } else {
                    astNode.firstChildNode
                }
            }
            return false
        }

        private fun endsWithTemplate(astNode: ASTNode?): Boolean {
            var astNode = astNode
            while (astNode != null) {
                if (astNode is OuterLanguageElement) return true
                astNode = astNode.lastChildNode
            }
            return false
        }

        private fun computeKeyValuePairIndent(node: ASTNode): Indent? {
            val parentType = PsiUtilCore.getElementType(node.treeParent)
            val grandParentType =
                if (parentType == null) null else PsiUtilCore.getElementType(node.treeParent.treeParent)
            val grandParentIsDocument = grandParentType === GameCacheConfigElementTypes.DOCUMENT
            return if (parentType === GameCacheConfigElementTypes.HASH) {
                // {
                //   key: value
                // }
                Indent.getNormalIndent()
            } else if (grandParentIsDocument) {
                // ---
                // key: value
                SAME_AS_PARENT_INDENT
            } else if (parentType === GameCacheConfigElementTypes.SEQUENCE_ITEM) {
                // [
                //   a: x,
                //   b: y
                // ]
                Indent.getNoneIndent()
            } else {
                // - - a: x
                //     b: y
                DIRECT_NORMAL_INDENT
            }
        }

        private fun getPreviousNonBlankNode(node: ASTNode): ASTNode? {
            var node: ASTNode? = node
            do {
                node = TreeUtil.prevLeaf(node)
                if (!GameCacheConfigElementTypes.BLANK_ELEMENTS.contains(PsiUtilCore.getElementType(node))) {
                    return node
                }
            } while (node != null)
            return null
        }
    }
}