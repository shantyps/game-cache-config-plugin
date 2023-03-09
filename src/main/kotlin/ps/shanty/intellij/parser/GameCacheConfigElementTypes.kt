package ps.shanty.intellij.parser

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.jetbrains.yaml.YAMLLanguage
import org.jetbrains.yaml.YAMLTokenTypes

object GameCacheConfigElementTypes {
    var FILE = IFileElementType(GameCacheConfigLanguage.INSTANCE)

    var DOCUMENT = GameCacheConfigElementType("Game Cache Document ---")

    var KEY_VALUE_PAIR = GameCacheConfigElementType("Game Cache Key value pair")

    //GameCacheConfigElementType VALUE = new GameCacheConfigElementType("Value");
    var HASH = GameCacheConfigElementType("Game Cache Hash")
    var ARRAY = GameCacheConfigElementType("Game Cache Array")
    var SEQUENCE_ITEM = GameCacheConfigElementType("Game Cache Sequence item")
    var COMPOUND_VALUE = GameCacheConfigElementType("Game Cache Compound value")
    var MAPPING = GameCacheConfigElementType("Game Cache Mapping")
    var SEQUENCE = GameCacheConfigElementType("Game Cache Sequence")
    var SCALAR_LIST_VALUE = GameCacheConfigElementType("Game Cache Scalar list value")
    var SCALAR_TEXT_VALUE = GameCacheConfigElementType("Game Cache Scalar text value")
    var SCALAR_PLAIN_VALUE = GameCacheConfigElementType("Game Cache Scalar plain style")
    var SCALAR_QUOTED_STRING = GameCacheConfigElementType("Game Cache Scalar quoted string")
    var ANCHOR_NODE = GameCacheConfigElementType("Game Cache Anchor node")
    var ALIAS_NODE = GameCacheConfigElementType("Game Cache Alias node")

    var BLOCK_SCALAR_ITEMS = TokenSet.create(
        YAMLTokenTypes.SCALAR_LIST,
        YAMLTokenTypes.SCALAR_TEXT
    )

    var TEXT_SCALAR_ITEMS = TokenSet.create(
        YAMLTokenTypes.SCALAR_STRING,
        YAMLTokenTypes.SCALAR_DSTRING,
        YAMLTokenTypes.TEXT
    )

    var SCALAR_ITEMS = TokenSet.orSet(BLOCK_SCALAR_ITEMS, TEXT_SCALAR_ITEMS)

    var SCALAR_VALUES = TokenSet.orSet(
        SCALAR_ITEMS, TokenSet.create(
            SCALAR_LIST_VALUE
        )
    )

    var EOL_ELEMENTS = TokenSet.create(
        YAMLTokenTypes.EOL,
        YAMLTokenTypes.SCALAR_EOL
    )

    var SPACE_ELEMENTS = TokenSet.orSet(
        EOL_ELEMENTS, TokenSet.create(
            YAMLTokenTypes.WHITESPACE,
            TokenType.WHITE_SPACE,
            YAMLTokenTypes.INDENT
        )
    )

    var BLANK_ELEMENTS = TokenSet.orSet(
        SPACE_ELEMENTS, TokenSet.create(
            YAMLTokenTypes.COMMENT
        )
    )

    var CONTAINERS = TokenSet.create(
        SCALAR_LIST_VALUE,
        SCALAR_TEXT_VALUE,
        DOCUMENT,
        SEQUENCE,
        MAPPING,
        SCALAR_QUOTED_STRING,
        SCALAR_PLAIN_VALUE
    )

    var BRACKETS = TokenSet.create(
        YAMLTokenTypes.LBRACE,
        YAMLTokenTypes.RBRACE,
        YAMLTokenTypes.LBRACKET,
        YAMLTokenTypes.RBRACKET
    )

    var DOCUMENT_BRACKETS = TokenSet.create(
        YAMLTokenTypes.DOCUMENT_MARKER,
        YAMLTokenTypes.DOCUMENT_END
    )

    var TOP_LEVEL = TokenSet.create(
        GameCacheConfigElementTypes.FILE,
        DOCUMENT
    )

    var INCOMPLETE_BLOCKS = TokenSet.create(
        MAPPING,
        SEQUENCE,
        COMPOUND_VALUE,
        SCALAR_LIST_VALUE,
        SCALAR_TEXT_VALUE
    )
}