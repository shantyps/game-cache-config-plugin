package ps.shanty.intellij.snt.psi.impl

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.lang.properties.parsing.PropertiesTokenTypes
import com.intellij.lang.properties.psi.PropertyStub
import com.intellij.lang.properties.psi.impl.PropertyImpl
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.stubs.*
import com.intellij.util.CharTable
import ps.shanty.intellij.snt.SNTElementTypes
import ps.shanty.intellij.snt.SNTKeyIndex
import ps.shanty.intellij.snt.psi.SNTEntry
import java.io.IOException

internal class SNTEntryStubElementType : ILightStubElementType<PropertyStub, SNTEntry>("PROPERTY", SNTElementTypes.LANG) {
    override fun createPsi(stub: PropertyStub): SNTEntry {
        return SNTEntryImpl(stub, this)
    }

    override fun createStub(psi: SNTEntry, parentStub: StubElement<*>?): PropertyStub {
        return SNTEntryStubImpl(parentStub, psi.key!!)
    }

    override fun getExternalId(): String {
        return "snt.entry"
    }

    @Throws(IOException::class)
    override fun serialize(stub: PropertyStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.key)
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): PropertyStub {
        return SNTEntryStubImpl(parentStub, dataStream.readNameString()!!)
    }

    override fun indexStub(stub: PropertyStub, sink: IndexSink) {
        sink.occurrence(SNTKeyIndex.KEY, PropertyImpl.unescape(stub.key))
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): PropertyStub {
        val keyNode = LightTreeUtil.firstChildOfType(tree, node, PropertiesTokenTypes.KEY_CHARACTERS)
        val key = intern(
            tree.charTable,
            keyNode!!
        )
        return SNTEntryStubImpl(parentStub, key)
    }

    companion object {
        fun intern(table: CharTable, node: LighterASTNode): String {
            assert(node is LighterASTTokenNode) { node }
            return table.intern((node as LighterASTTokenNode).text).toString()
        }
    }
}