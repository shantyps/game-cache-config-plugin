package ps.shanty.intellij.mod.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.TokenSet
import org.jetbrains.yaml.psi.YAMLDocument
import ps.shanty.intellij.mod.ModElementTypes
import ps.shanty.intellij.mod.ModLanguage
import ps.shanty.intellij.mod.ModFileType

class ModFileImpl(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ModLanguage.INSTANCE),
    ModFile {

    override fun getFileType(): FileType {
        return ModFileType.MOD
    }

    override fun toString(): String {
        return "Game Cache Config file"
    }

    override fun getDocuments(): List<YAMLDocument> {
        val result = ArrayList<YAMLDocument>()
        for (node in node.getChildren(TokenSet.create(ModElementTypes.DOCUMENT))) {
            result.add(node.psi as YAMLDocument)
        }
        return result
    }
}