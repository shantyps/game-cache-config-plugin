package ps.shanty.intellij.data

import com.intellij.psi.PsiElement

class ShantyNameTableEntries {

    var tableEntryForName = mapOf<String, List<PsiElement>>()

    companion object {
        val INSTANCE = ShantyNameTableEntries()
    }
}