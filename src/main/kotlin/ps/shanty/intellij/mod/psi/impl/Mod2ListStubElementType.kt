package ps.shanty.intellij.mod.psi.impl

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.stubs.*
import ps.shanty.intellij.mod.Mod2ElementTypes
import ps.shanty.intellij.mod.psi.Mod2List
import ps.shanty.intellij.mod.psi.Mod2ListStub
import java.io.IOException

class Mod2ListStubElementType : ILightStubElementType<Mod2ListStub, Mod2List>("MOD2_LIST", Mod2ElementTypes.LANG) {
    override fun createPsi(stub: Mod2ListStub): Mod2List {
        return ModBlockMappingImpl(stub)
    }

    override fun createStub(psi: Mod2List, parentStub: StubElement<*>?): Mod2ListStub {
        return Mod2ListStubImpl(parentStub)
    }

    override fun getExternalId(): String {
        return "mod2.entrylist"
    }

    @Throws(IOException::class)
    override fun serialize(stub: Mod2ListStub, dataStream: StubOutputStream) {
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): Mod2ListStub {
        return Mod2ListStubImpl(parentStub)
    }

    override fun indexStub(stub: Mod2ListStub, sink: IndexSink) {}
    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Mod2ListStub {
        return Mod2ListStubImpl(parentStub)
    }
}