package ps.shanty.intellij.snt.formatter

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.properties.PropertiesBundle
import com.intellij.lang.properties.PropertiesHighlightingLexer
import com.intellij.lang.properties.parsing.PropertiesTokenTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.StringEscapesTokenTypes
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.Nls
import java.util.*
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors

open class SNTHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer {
        return PropertiesHighlightingLexer()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        val type = SNTComponent.getByTokenType(tokenType)
        var key: TextAttributesKey? = null
        if (type != null) {
            key = type.textAttributesKey
        }
        return pack(key)
    }

    enum class SNTComponent(
        val textAttributesKey: TextAttributesKey,
        val messagePointer: Supplier<String>,
        val tokenType: IElementType
    ) {
        PROPERTY_KEY(
            TextAttributesKey.createTextAttributesKey("PROPERTIES.KEY", DefaultLanguageHighlighterColors.KEYWORD),
            PropertiesBundle.messagePointer("options.properties.attribute.descriptor.property.key"),
            PropertiesTokenTypes.KEY_CHARACTERS
        ),
        PROPERTY_VALUE(
            TextAttributesKey.createTextAttributesKey("PROPERTIES.VALUE", DefaultLanguageHighlighterColors.STRING),
            PropertiesBundle.messagePointer("options.properties.attribute.descriptor.property.value"),
            PropertiesTokenTypes.VALUE_CHARACTERS
        ),
        PROPERTY_COMMENT(
            TextAttributesKey.createTextAttributesKey(
                "PROPERTIES.LINE_COMMENT",
                DefaultLanguageHighlighterColors.LINE_COMMENT
            ),
            PropertiesBundle.messagePointer("options.properties.attribute.descriptor.comment"),
            PropertiesTokenTypes.END_OF_LINE_COMMENT
        ),
        PROPERTY_KEY_VALUE_SEPARATOR(
            TextAttributesKey.createTextAttributesKey(
                "PROPERTIES.KEY_VALUE_SEPARATOR",
                DefaultLanguageHighlighterColors.OPERATION_SIGN
            ),
            PropertiesBundle.messagePointer("options.properties.attribute.descriptor.key.value.separator"),
            PropertiesTokenTypes.KEY_VALUE_SEPARATOR
        ),
        PROPERTIES_VALID_STRING_ESCAPE(
            TextAttributesKey.createTextAttributesKey(
                "PROPERTIES.VALID_STRING_ESCAPE",
                DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
            ),
            PropertiesBundle.messagePointer("options.properties.attribute.descriptor.valid.string.escape"),
            StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
        ),
        PROPERTIES_INVALID_STRING_ESCAPE(
            TextAttributesKey.createTextAttributesKey(
                "PROPERTIES.INVALID_STRING_ESCAPE",
                DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE
            ),
            PropertiesBundle.messagePointer("options.properties.attribute.descriptor.invalid.string.escape"),
            StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN
        );

        companion object {
            private var elementTypeToComponent: Map<IElementType, SNTComponent>? = null
            private var textAttributeKeyToComponent: Map<TextAttributesKey, SNTComponent>? = null

            init {
                elementTypeToComponent = Arrays.stream(values())
                    .collect(Collectors.toMap({ obj: SNTComponent -> obj.tokenType }, Function.identity()))
                textAttributeKeyToComponent = Arrays.stream(values())
                    .collect(
                        Collectors.toMap(
                            { obj: SNTComponent -> obj.textAttributesKey },
                            Function.identity()
                        )
                    )
            }

            fun getByTokenType(tokenType: IElementType): SNTComponent? {
                return elementTypeToComponent!![tokenType]
            }

            fun getByTextAttribute(textAttributesKey: TextAttributesKey): SNTComponent? {
                return textAttributeKeyToComponent!![textAttributesKey]
            }

            fun getDisplayName(key: TextAttributesKey): @Nls String? {
                val component = getByTextAttribute(key)
                    ?: return null
                return component.messagePointer.get()
            }

            fun getSeverity(key: TextAttributesKey): @Nls HighlightSeverity? {
                val component = getByTextAttribute(key)
                return if (component == PROPERTIES_INVALID_STRING_ESCAPE) HighlightSeverity.WARNING else null
            }
        }
    }
}