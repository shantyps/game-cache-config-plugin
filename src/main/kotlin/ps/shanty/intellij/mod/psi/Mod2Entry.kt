package ps.shanty.intellij.mod.psi

import com.intellij.psi.StubBasedPsiElement

interface Mod2Entry : StubBasedPsiElement<Mod2EntryStub> {
    fun getKeyText(): String
}