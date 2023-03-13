package ps.shanty.intellij.snt.parser

import com.intellij.lang.*
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.lang.properties.PropertiesBundle
import com.intellij.lang.properties.parsing.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.util.Ref
import com.intellij.psi.tree.IElementType
import com.intellij.util.ThreeState
import com.intellij.util.TripleFunction
import com.intellij.util.diff.FlyweightCapableTreeStructure
import ps.shanty.intellij.snt.SNTBundle
import ps.shanty.intellij.snt.SNTElementTypes

class SNTParser : PropertiesParser() {

    override fun doParse(root: IElementType?, builder: PsiBuilder) {
        builder.putUserData(PsiBuilderImpl.CUSTOM_COMPARATOR, MATCH_BY_KEY)
        val rootMarker = builder.mark()
        val propertiesList = builder.mark()
        if (builder.eof()) {
            propertiesList.setCustomEdgeTokenBinders(
                WhitespacesBinders.GREEDY_LEFT_BINDER,
                WhitespacesBinders.GREEDY_RIGHT_BINDER
            )
        } else {
            propertiesList.setCustomEdgeTokenBinders(WhitespacesBinders.GREEDY_LEFT_BINDER, null)
        }
        while (!builder.eof()) {
            parseProperty(builder)
        }
        propertiesList.done(SNTElementTypes.PROPERTIES_LIST)
        rootMarker.done(root!!)
    }

    private fun parseProperty(builder: PsiBuilder) {
        if (builder.tokenType === PropertiesTokenTypes.KEY_CHARACTERS) {
            val prop = builder.mark()
            parseKey(builder)
            if (builder.tokenType === PropertiesTokenTypes.KEY_VALUE_SEPARATOR) {
                parseKeyValueSeparator(builder)
            }
            if (builder.tokenType === PropertiesTokenTypes.VALUE_CHARACTERS) {
                parseValue(builder)
            }
            prop.done(SNTElementTypes.PROPERTY)
        } else {
            builder.advanceLexer()
            builder.error(SNTBundle.message("property.key.expected.parsing.error.message"))
        }
    }

    private fun parseKeyValueSeparator(builder: PsiBuilder) {
        LOG.assertTrue(builder.tokenType === PropertiesTokenTypes.KEY_VALUE_SEPARATOR)
        builder.advanceLexer()
    }

    private fun parseValue(builder: PsiBuilder) {
        if (builder.tokenType === PropertiesTokenTypes.VALUE_CHARACTERS) {
            builder.advanceLexer()
        }
    }

    private fun parseKey(builder: PsiBuilder) {
        LOG.assertTrue(builder.tokenType === PropertiesTokenTypes.KEY_CHARACTERS)
        builder.advanceLexer()
    }

    companion object {
        private val LOG = Logger.getInstance(SNTParser::class.java)

        private val MATCH_BY_KEY =
            TripleFunction { oldNode: ASTNode, newNode: LighterASTNode, structure: FlyweightCapableTreeStructure<LighterASTNode> ->
                if (oldNode.elementType === SNTElementTypes.PROPERTY) {
                    val oldName =
                        oldNode.findChildByType(PropertiesTokenTypes.KEY_CHARACTERS)
                    if (oldName != null) {
                        val oldNameStr = oldName.chars
                        val newNameStr =
                            findKeyCharacters(
                                newNode,
                                structure
                            )
                        if (!Comparing.equal(oldNameStr, newNameStr)) {
                            return@TripleFunction ThreeState.NO
                        }
                    }
                }
                ThreeState.UNSURE
            }

        private fun findKeyCharacters(
            newNode: LighterASTNode,
            structure: FlyweightCapableTreeStructure<LighterASTNode>
        ): CharSequence? {
            val childrenRef = Ref.create<Array<LighterASTNode>?>(null)
            val childrenCount = structure.getChildren(newNode, childrenRef)
            val children = childrenRef.get()
            return try {
                for (aChildren in children!!) {
                    if (aChildren.tokenType === PropertiesTokenTypes.KEY_CHARACTERS) {
                        return (aChildren as LighterASTTokenNode).text
                    }
                }
                null
            } finally {
                structure.disposeChildren(children, childrenCount)
            }
        }
    }
}