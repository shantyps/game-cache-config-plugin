package ps.shanty.intellij.mod.psi.impl

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.stubs.*
import com.intellij.util.CharTable
import org.jetbrains.yaml.YAMLTokenTypes
import ps.shanty.intellij.mod.Mod2ElementTypes
import ps.shanty.intellij.mod.Mod2KeyIndex
import ps.shanty.intellij.mod.ModElementTypes
import ps.shanty.intellij.mod.psi.*
import java.io.IOException

class Mod2EntryStubElementType : ILightStubElementType<Mod2EntryStub, Mod2Entry>("MOD2_ENTRY", Mod2ElementTypes.LANG) {
    override fun createPsi(stub: Mod2EntryStub): Mod2Entry {
        return ModKeyValueImpl(stub, this)
    }

    override fun createStub(psi: Mod2Entry, parentStub: StubElement<*>?): Mod2EntryStub {
        return Mod2EntryStubImpl(parentStub, psi.getKeyText())
    }

    override fun getExternalId(): String {
        return "mod2.entry"
    }

    @Throws(IOException::class)
    override fun serialize(stub: Mod2EntryStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getKey())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): Mod2EntryStub {
        return Mod2EntryStubImpl(parentStub, StringUtil.unquoteString(dataStream.readNameString()!!))
    }

    override fun indexStub(stub: Mod2EntryStub, sink: IndexSink) {
        if (stub.parentStub is Mod2ListStub && stub.parentStub.parentStub !is Mod2DocumentStub || stub.parentStub !is Mod2ListStub) {
            return
        }
        // note: not sure if this should be stub.getKey() or some sort of escaping
        sink.occurrence(Mod2KeyIndex.KEY, stub.getKey())
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Mod2EntryStub {
        val textNode = getTextNode(tree, node)
        val keyNode = LightTreeUtil.firstChildOfType(tree, textNode, YAMLTokenTypes.SCALAR_KEY)
        val key = intern(
            tree.charTable,
            keyNode!!
        )
        return Mod2EntryStubImpl(parentStub, StringUtil.unquoteString(key))
    }

    private fun getTextNode(tree: LighterAST, node: LighterASTNode): LighterASTNode? {
        return ModElementTypes.PLAIN_SCALAR_TEXTS.types
            .map { LightTreeUtil.firstChildOfType(tree, node, ModElementTypes.SCALAR_TEXT_VALUE) }
            .firstOrNull()
    }

    companion object {
        fun intern(table: CharTable, node: LighterASTNode): String {
            assert(node is LighterASTTokenNode) { node }
            return table.intern((node as LighterASTTokenNode).text).toString()
        }
    }
}