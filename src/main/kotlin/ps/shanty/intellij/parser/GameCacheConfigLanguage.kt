package ps.shanty.intellij.parser

import com.intellij.lang.Language
import org.jetbrains.yaml.YAMLLanguage

class GameCacheConfigLanguage : Language("gameCacheConfig") {
    override fun getDisplayName(): String {
        return "Game Cache Config"
    }

    companion object {
        val INSTANCE = GameCacheConfigLanguage()
    }
}