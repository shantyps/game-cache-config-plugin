package ps.shanty.intellij.mod

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class ModFileType : LanguageFileType(ModLanguage.INSTANCE) {

    override fun getName(): String {
        return "Game Cache Config File"
    }

    override fun getDescription(): String {
        return "Game Cache Config File Type"
    }

    override fun getDefaultExtension(): String {
        return DEFAULT_EXTENSION
    }

    override fun getIcon(): Icon {
        return ModIcons.LOGO
    }

    companion object {
        @JvmStatic
        val MOD = ModFileType()
        val DEFAULT_EXTENSION = "mod"
    }
}