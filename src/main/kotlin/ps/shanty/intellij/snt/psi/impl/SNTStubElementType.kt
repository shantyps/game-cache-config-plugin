package ps.shanty.intellij.snt.psi.impl

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.stubs.*
import ps.shanty.intellij.snt.SNTElementTypes
import ps.shanty.intellij.snt.psi.SNT
import ps.shanty.intellij.snt.psi.SNTStub
import java.io.IOException

class SNTStubElementType : ILightStubElementType<SNTStub, SNT>("SNT_LIST", SNTElementTypes.LANG) {
    override fun createPsi(stub: SNTStub): SNT {
        return SNTImpl(stub)
    }

    override fun createStub(psi: SNT, parentStub: StubElement<*>?): SNTStub {
        return SNTStubImpl(parentStub)
    }

    override fun getExternalId(): String {
        return "snt.sntlist"
    }

    @Throws(IOException::class)
    override fun serialize(stub: SNTStub, dataStream: StubOutputStream) {
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): SNTStub {
        return SNTStubImpl(parentStub)
    }

    override fun indexStub(stub: SNTStub, sink: IndexSink) {}
    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): SNTStub {
        return SNTStubImpl(parentStub)
    }
}