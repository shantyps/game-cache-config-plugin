package ps.shanty.intellij.snt.psi.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import ps.shanty.intellij.snt.SNTElementTypes
import ps.shanty.intellij.snt.psi.SNT
import ps.shanty.intellij.snt.psi.SNTStub

class SNTStubImpl(parent: StubElement<*>?) : StubBase<SNT>(parent, SNTElementTypes.PROPERTIES_LIST), SNTStub
