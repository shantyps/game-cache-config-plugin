// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package ps.shanty.intellij.mod.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.ItemPresentationProviders
import com.intellij.openapi.util.Iconable.IconFlags
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.IncorrectOperationException
import com.intellij.util.ObjectUtils
import com.intellij.util.PlatformIcons
import org.jetbrains.annotations.NonNls
import org.jetbrains.yaml.*
import org.jetbrains.yaml.psi.*
import org.jetbrains.yaml.psi.impl.YAMLPsiElementImpl
import ps.shanty.intellij.mod.psi.ModKeyValue
import javax.swing.Icon

class ModKeyValueImpl(node: ASTNode) : YAMLPsiElementImpl(node), ModKeyValue {

    override fun toString(): String {
        return "Game Cache Config key value"
    }

    override fun getKey(): PsiElement? {
        val colon = findChildByType<PsiElement>(YAMLTokenTypes.COLON) ?: return null
        var node = colon.node
        do {
            node = node!!.treePrev
        } while (YAMLElementTypes.BLANK_ELEMENTS.contains(PsiUtilCore.getElementType(node)))
        return if (node == null || PsiUtilCore.getElementType(node) === YAMLTokenTypes.QUESTION) {
            null
        } else {
            node.psi
        }
    }

    override fun getParentMapping(): YAMLMapping? {
        return ObjectUtils.tryCast(super.getParent(), YAMLMapping::class.java)
    }

    override fun getName(): String {
        return keyText
    }

    override fun getKeyText(): String {
        val keyElement = key ?: return ""
        if (keyElement is YAMLScalar) {
            return keyElement.textValue
        }
        if (keyElement is YAMLCompoundValue) {
            return keyElement.textValue
        }
        val text = keyElement.text
        return StringUtil.unquoteString(text)
    }

    override fun getValue(): YAMLValue? {
        var child = lastChild
        while (child != null) {
            if (PsiUtilCore.getElementType(child) === YAMLTokenTypes.COLON) {
                return null
            }
            if (child is YAMLValue) {
                return child
            }
            child = child.prevSibling
        }
        return null
    }

    override fun getValueText(): String {
        val value = value
        if (value is YAMLScalar) {
            return value.textValue
        } else if (value is YAMLCompoundValue) {
            return value.textValue
        }
        return ""
    }

    override fun setValue(value: YAMLValue) {
        adjustWhitespaceToContentType(value is YAMLScalar)
        if (getValue() != null) {
            getValue()!!.replace(value)
            return
        }
        val generator = YAMLElementGenerator.getInstance(project)
        if (isExplicit) {
            if (findChildByType<PsiElement?>(YAMLTokenTypes.COLON) == null) {
                add(generator.createColon())
                add(generator.createSpace())
                add(value)
            }
        } else {
            add(value)
        }
    }

    private fun adjustWhitespaceToContentType(isScalar: Boolean) {
        assert(key != null)
        var key = key
        if (key!!.nextSibling != null && key.nextSibling.node.elementType === YAMLTokenTypes.COLON) {
            key = key.nextSibling
        }
        while (key!!.nextSibling != null && key.nextSibling !is YAMLValue) {
            key.nextSibling.delete()
        }
        val generator = YAMLElementGenerator.getInstance(project)
        if (isScalar) {
            addAfter(generator.createSpace(), key)
        } else {
            val indent = YAMLUtil.getIndentToThisElement(this)
            addAfter(generator.createIndent(indent + 2), key)
            addAfter(generator.createEol(), key)
        }
    }

    override fun getElementIcon(@IconFlags flags: Int): Icon {
        return YAML_KEY_ICON
    }

    override fun getPresentation(): ItemPresentation {
        val custom = ItemPresentationProviders.getItemPresentation(this)
        if (custom != null) {
            return custom
        }
        val yamlFile = containingFile as YAMLFile
        val value: PsiElement? = value
        return object : ItemPresentation {
            override fun getPresentableText(): String? {
                if (value is YAMLScalar) {
                    val presentation = value.presentation
                    return if (presentation != null) presentation.presentableText else valueText
                }
                return name
            }

            override fun getLocationString(): String {
                return yamlFile.name
            }

            override fun getIcon(open: Boolean): Icon? {
                return this@ModKeyValueImpl.getIcon(0)
            }
        }
    }

    @Throws(IncorrectOperationException::class)
    override fun setName(newName: @NonNls String): PsiElement {
        if (newName == name) {
            throw IncorrectOperationException(YAMLBundle.message("rename.same.name"))
        }
        val topKeyValue = YAMLElementGenerator.getInstance(project).createYamlKeyValue(newName, "Foo")

        val value = value
        check(!(value == null || topKeyValue.value == null))
        value.replace(topKeyValue.value!!)
        return this
    }

    /**
     * Provide reference contributor with given method registerReferenceProviders implementation:
     * registrar.registerReferenceProvider(PlatformPatterns.psiElement(YAMLKeyValue.class), ReferenceProvider);
     */
    override fun getReferences(): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this)
    }

    private val isExplicit: Boolean
        private get() {
            val child = node.firstChildNode
            return child != null && child.elementType === YAMLTokenTypes.QUESTION
        }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is YamlPsiElementVisitor) {
            visitor.visitKeyValue(this)
        } else {
            super.accept(visitor)
        }
    }

    override fun getNavigationElement(): PsiElement {
        return this
    }

    companion object {
        val YAML_KEY_ICON = PlatformIcons.PROPERTY_ICON
    }
}