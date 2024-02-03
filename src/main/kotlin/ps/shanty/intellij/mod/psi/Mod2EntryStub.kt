package ps.shanty.intellij.mod.psi

import com.intellij.psi.stubs.StubElement

interface Mod2EntryStub : StubElement<Mod2Entry> {
    fun getKey(): String
}