package ps.shanty.intellij.mod

import com.intellij.openapi.fileTypes.LanguageFileType
import ps.shanty.intellij.PluginIcons
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
        return PluginIcons.LOGO
    }

    companion object {
        @JvmStatic
        val MOD = ModFileType()
        const val DEFAULT_EXTENSION = "mod"
        const val ALL_EXTENSIONS = "mod;bas;enum;hunt;inv;loc;maparea;maplabel;mapfunc;npc;ns;obj;param;seq;struct;varbit;varclan;varclansetting;varclient;vardouble;varlong;varp;varstring"
    }
}