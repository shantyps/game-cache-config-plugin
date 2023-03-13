package ps.shanty.intellij.snt

import com.intellij.lang.Language

class SNTLanguage : Language("snt") {
    override fun getDisplayName(): String {
        return "Shanty Name Table"
    }

    companion object {
        val INSTANCE = SNTLanguage()
    }
}