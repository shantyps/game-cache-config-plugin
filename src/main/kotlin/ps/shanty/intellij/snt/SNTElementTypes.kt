package ps.shanty.intellij.snt

import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.LighterASTNode
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.ILightStubFileElementType
import com.intellij.util.diff.FlyweightCapableTreeStructure
import ps.shanty.intellij.snt.parser.SNTParser
import ps.shanty.intellij.snt.psi.impl.SNTEntryStubElementType
import ps.shanty.intellij.snt.psi.impl.SNTStubElementType

interface SNTElementTypes {
    companion object {
        @JvmField
        val LANG = SNTLanguage.INSTANCE

        @JvmField
        val FILE = object : ILightStubFileElementType<PsiFileStub<PsiFile>>(LANG) {
            override fun parseContentsLight(chameleon: ASTNode): FlyweightCapableTreeStructure<LighterASTNode> {
                val psi = chameleon.psi ?: error("Bad chameleon: $chameleon")
                val project = psi.project
                val factory = PsiBuilderFactory.getInstance()
                val builder = factory.createBuilder(project, chameleon)
                val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language) ?: error(this)
                val parser = SNTParser()
                return parser.parseLight(this, builder)
            }
        }

        @JvmField
        val PROPERTY: IStubElementType<*, *> = SNTEntryStubElementType()

        @JvmField
        val PROPERTIES_LIST = SNTStubElementType()
    }
}