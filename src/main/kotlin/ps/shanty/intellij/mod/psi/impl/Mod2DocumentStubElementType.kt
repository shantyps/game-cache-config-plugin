package ps.shanty.intellij.mod.psi.impl

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.stubs.*
import ps.shanty.intellij.mod.Mod2ElementTypes
import ps.shanty.intellij.mod.psi.Mod2Document
import ps.shanty.intellij.mod.psi.Mod2DocumentStub
import java.io.IOException

class Mod2DocumentStubElementType : ILightStubElementType<Mod2DocumentStub, Mod2Document>("MOD2_DOCUMENT", Mod2ElementTypes.LANG) {
    override fun createPsi(stub: Mod2DocumentStub): Mod2Document {
        return ModDocumentImpl(stub)
    }

    override fun createStub(psi: Mod2Document, parentStub: StubElement<*>?): Mod2DocumentStub {
        return Mod2DocumentStubImpl(parentStub)
    }

    override fun getExternalId(): String {
        return "mod2.document"
    }

    @Throws(IOException::class)
    override fun serialize(stub: Mod2DocumentStub, dataStream: StubOutputStream) {
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): Mod2DocumentStub {
        return Mod2DocumentStubImpl(parentStub)
    }

    override fun indexStub(stub: Mod2DocumentStub, sink: IndexSink) {}
    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Mod2DocumentStub {
        return Mod2DocumentStubImpl(parentStub)
    }
}