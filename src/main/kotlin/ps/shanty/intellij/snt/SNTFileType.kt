package ps.shanty.intellij.snt

import com.intellij.openapi.fileTypes.LanguageFileType
import ps.shanty.intellij.PluginIcons
import javax.swing.Icon

class SNTFileType : LanguageFileType(SNTLanguage.INSTANCE) {

    override fun getName(): String {
        return "Game Cache Config Table File"
    }

    override fun getDescription(): String {
        return "Game Cache Config Table File Type"
    }

    override fun getDefaultExtension(): String {
        return DEFAULT_EXTENSION
    }

    override fun getIcon(): Icon {
        return PluginIcons.CHART
    }

    companion object {
        @JvmStatic
        val SNT = SNTFileType()
        val DEFAULT_EXTENSION = "snt"
    }
}