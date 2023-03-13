package ps.shanty.intellij.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.TokenSet
import org.jetbrains.yaml.YAMLElementTypes
import org.jetbrains.yaml.YAMLLanguage
import org.jetbrains.yaml.psi.YAMLDocument

class GameCacheConfigFileImpl(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, GameCacheConfigLanguage.INSTANCE), GameCacheConfigFile {

    override fun getFileType(): FileType {
        return GameCacheConfigFileType.GAME_CACHE_CONFIG
    }

    override fun toString(): String {
        return "Game Cache Config file"
    }

    override fun getDocuments(): List<YAMLDocument> {
        val result = ArrayList<YAMLDocument>()
        for (node in node.getChildren(TokenSet.create(GameCacheConfigElementTypes.DOCUMENT))) {
            result.add(node.psi as YAMLDocument)
        }
        return result
    }
}