package ps.shanty.intellij.mod

import com.intellij.lang.Language

class ModLanguage : Language("mod") {
    override fun getDisplayName(): String {
        return "Game Cache Config"
    }

    companion object {
        val INSTANCE = ModLanguage()
    }
}