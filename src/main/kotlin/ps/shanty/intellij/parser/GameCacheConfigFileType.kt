package ps.shanty.intellij.parser

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.annotations.NonNls
import org.jetbrains.yaml.YAMLBundle
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.YAMLLanguage
import javax.swing.Icon

class GameCacheConfigFileType : LanguageFileType(GameCacheConfigLanguage.INSTANCE) {

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
        return GameCacheConfigIcons.LOGO
    }

    companion object {
        @JvmStatic
        val GAME_CACHE_CONFIG = GameCacheConfigFileType()
        val DEFAULT_EXTENSION = "mod"
    }
}