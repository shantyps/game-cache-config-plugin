package ps.shanty.intellij.mod.psi.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import ps.shanty.intellij.mod.Mod2ElementTypes
import ps.shanty.intellij.mod.psi.Mod2Entry
import ps.shanty.intellij.mod.psi.Mod2EntryStub

class Mod2EntryStubImpl(parent: StubElement<*>?, private val myKey: String) : StubBase<Mod2Entry>(parent, Mod2ElementTypes.KEY_VALUE_PAIR), Mod2EntryStub {
    override fun getKey(): String {
        return myKey
    }
}