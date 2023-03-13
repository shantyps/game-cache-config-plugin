package ps.shanty.intellij.snt

import com.intellij.psi.PsiElement

class SNTEntries {

    var tableEntryForName = mapOf<String, List<PsiElement>>()

    companion object {
        val INSTANCE = SNTEntries()
    }
}