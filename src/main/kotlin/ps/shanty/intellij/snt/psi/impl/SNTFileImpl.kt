package ps.shanty.intellij.snt.psi.impl

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.TokenSet
import org.jetbrains.yaml.psi.YAMLDocument
import ps.shanty.intellij.snt.SNTElementTypes
import ps.shanty.intellij.snt.SNTFileType
import ps.shanty.intellij.snt.SNTLanguage
import ps.shanty.intellij.snt.psi.SNTFile

class SNTFileImpl(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, SNTLanguage.INSTANCE), SNTFile {

    override fun getFileType(): FileType {
        return SNTFileType.SNT
    }

    override fun toString(): String {
        return "Game Cache Config file"
    }

    override fun getDocuments(): List<YAMLDocument> {
        val result = ArrayList<YAMLDocument>()
        for (node in node.getChildren(TokenSet.create(SNTElementTypes.DOCUMENT))) {
            result.add(node.psi as YAMLDocument)
        }
        return result
    }
}