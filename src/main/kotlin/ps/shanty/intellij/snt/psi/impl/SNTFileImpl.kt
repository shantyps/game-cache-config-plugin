package ps.shanty.intellij.snt.psi.impl

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTFactory
import com.intellij.lang.ASTNode
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.properties.*
import com.intellij.lang.properties.ResourceBundle
import com.intellij.lang.properties.parsing.PropertiesTokenTypes
import com.intellij.lang.properties.psi.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.tree.ChangeUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ArrayUtil
import com.intellij.util.IncorrectOperationException
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.annotations.NonNls
import com.intellij.lang.properties.psi.Property
import ps.shanty.intellij.snt.*
import ps.shanty.intellij.snt.psi.SNT
import ps.shanty.intellij.snt.psi.SNTElementFactory
import ps.shanty.intellij.snt.psi.SNTEntry
import ps.shanty.intellij.snt.psi.SNTFile
import ps.shanty.intellij.snt.util.SNTUtil
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

class SNTFileImpl(viewProvider: FileViewProvider?) : PsiFileBase(viewProvider!!, SNTLanguage.INSTANCE), SNTFile {
    override fun getFileType(): FileType {
        return SNTFileType.SNT
    }

    override fun toString(): @NonNls String {
        return "Shanty Name Table:$name"
    }

    override val properties: List<ISNTEntry>
        get() {
            val propertiesList: SNT?
            val stub = greenStub
            propertiesList = if (stub != null) {
                val propertiesListStub = stub.findChildStubByType(SNTElementTypes.PROPERTIES_LIST)
                propertiesListStub?.psi
            } else {
                PsiTreeUtil.findChildOfType(this, SNT::class.java)
            }
            return Collections.unmodifiableList<ISNTEntry>(
                PsiTreeUtil.getStubChildrenOfTypeAsList(
                    propertiesList,
                    SNTEntry::class.java
                )
            )
        }

    private val propertiesList: ASTNode?
        private get() = ArrayUtil.getFirstElement(node.getChildren(PROPERTIES_LIST_SET))

    override fun findPropertyByKey(key: String): ISNTEntry {
        return propertiesByKey(key).findFirst().orElse(null)
    }

    override fun findPropertiesByKey(key: String): List<ISNTEntry> {
        return propertiesByKey(key).collect(Collectors.toList())
    }

    override val locale: Locale
        get() = SNTUtil.getLocale(this)

    @Throws(IncorrectOperationException::class)
    override fun add(element: PsiElement): PsiElement {
        if (element is Property) {
            throw IncorrectOperationException("Use addProperty() instead")
        }
        return super.add(element)
    }

    @Throws(IncorrectOperationException::class)
    override fun addProperty(property: ISNTEntry): PsiElement {
        val position = findInsertionPosition(property)
        return addPropertyAfter(property, position)
    }

    @Throws(IncorrectOperationException::class)
    override fun addPropertyAfter(property: ISNTEntry, anchor: ISNTEntry?): PsiElement {
        val copy = ChangeUtil.copyToElement(property.psiElement)
        val properties = properties
        var anchorBefore =
            if (anchor == null) if (properties.isEmpty()) null else properties[0].psiElement.node else anchor.psiElement.node.treeNext
        if (anchorBefore != null && anchorBefore.elementType === TokenType.WHITE_SPACE) {
            anchorBefore = anchorBefore.treeNext
        }
        if (anchorBefore == null && haveToAddNewLine()) {
            insertLineBreakBefore(null)
        }
        propertiesList!!.addChild(copy, anchorBefore)
        anchorBefore?.let { insertLineBreakBefore(it) }
        return copy.psi
    }

    override fun addProperty(key: String, value: String, format: PropertyKeyValueFormat): ISNTEntry {
        return addProperty(SNTElementFactory.createProperty(project, key, value, null, format)) as ISNTEntry
    }

    override fun addPropertyAfter(key: String, value: String, anchor: ISNTEntry?): ISNTEntry {
        return addPropertyAfter(SNTElementFactory.createProperty(project, key, value, null), anchor) as ISNTEntry
    }

    private fun insertLineBreakBefore(anchorBefore: ASTNode?) {
        val propertiesList = propertiesList
        if (anchorBefore == null && propertiesList!!.firstChildNode == null) {
            node.addChild(ASTFactory.whitespace("\n"), propertiesList)
        } else {
            propertiesList!!.addChild(ASTFactory.whitespace("\n"), anchorBefore)
        }
    }

    private fun haveToAddNewLine(): Boolean {
        val propertiesList = propertiesList
        val lastChild = propertiesList!!.lastChildNode
        if (lastChild != null) {
            return !lastChild.text.endsWith("\n")
        }
        val prev = propertiesList.treePrev
        return prev == null || !PropertiesTokenTypes.WHITESPACES.contains(prev.elementType)
    }

    override val namesMap: Map<String, String>
        get() {
            val result = hashMapOf<String, String>()
            for (property in properties) {
                result[property.unescapedKey!!] = property.getValue()
            }
            return result
        }

    override val isAlphaSorted: Boolean
        get() = SNTUtil.isAlphaSorted(properties)

    private fun findInsertionPosition(property: ISNTEntry): ISNTEntry? {
        val properties = properties
        if (properties.isEmpty()) return null
        if (SNTUtil.isAlphaSorted(properties)) {
            val insertIndex = Collections.binarySearch(properties, property) { p1: ISNTEntry, p2: ISNTEntry ->
                val k1 = p1.key
                val k2 = p2.key
                LOG.assertTrue(k1 != null && k2 != null)
                java.lang.String.CASE_INSENSITIVE_ORDER.compare(k1, k2)
            }
            return if (insertIndex == -1) null else properties[if (insertIndex < 0) -insertIndex - 2 else insertIndex]
        }
        return ContainerUtil.getLastItem(properties)
    }

    private fun propertiesByKey(key: String): Stream<out ISNTEntry> {
        return if (shouldReadIndex()) {
            SNTKeyIndex.instance[key, project, GlobalSearchScope.fileScope(this)].stream()
        } else properties.stream().filter { p: ISNTEntry -> key == p.unescapedKey }
        // see PropertiesElementFactory.createPropertiesFile(Project, Properties, String)
    }

    private fun shouldReadIndex(): Boolean {
        val project = project
        if (DumbService.isDumb(project)) return false
        val file = virtualFile
        return file != null && ProjectFileIndex.getInstance(project)
            .isInContent(file) && !InjectedLanguageManager.getInstance(project).isInjectedFragment(
            containingFile
        )
    }

    companion object {
        private val LOG = Logger.getInstance(SNTFileImpl::class.java)
        private val PROPERTIES_LIST_SET = TokenSet.create(SNTElementTypes.PROPERTIES_LIST)
    }
}