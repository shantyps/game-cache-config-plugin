package ps.shanty.intellij.mod.psi.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import ps.shanty.intellij.mod.Mod2ElementTypes
import ps.shanty.intellij.mod.psi.Mod2Document
import ps.shanty.intellij.mod.psi.Mod2DocumentStub

class Mod2DocumentStubImpl(parent: StubElement<*>?) : StubBase<Mod2Document>(parent, Mod2ElementTypes.DOCUMENT), Mod2DocumentStub
