package ps.shanty.intellij.mod.psi.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import ps.shanty.intellij.mod.Mod2ElementTypes
import ps.shanty.intellij.mod.psi.Mod2List
import ps.shanty.intellij.mod.psi.Mod2ListStub

class Mod2ListStubImpl(parent: StubElement<*>?) : StubBase<Mod2List>(parent, Mod2ElementTypes.MAPPING), Mod2ListStub
