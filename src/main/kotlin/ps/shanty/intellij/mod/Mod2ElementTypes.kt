package ps.shanty.intellij.mod

import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.LighterASTNode
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.ILightStubFileElementType
import com.intellij.util.diff.FlyweightCapableTreeStructure
import ps.shanty.intellij.mod.parser.ModParser
import ps.shanty.intellij.mod.psi.impl.Mod2DocumentStubElementType
import ps.shanty.intellij.mod.psi.impl.Mod2EntryStubElementType
import ps.shanty.intellij.mod.psi.impl.Mod2ListStubElementType

interface Mod2ElementTypes {
    companion object {
        @JvmField
        val LANG = ModLanguage.INSTANCE

        @JvmField
        val FILE = object : ILightStubFileElementType<PsiFileStub<PsiFile>>(LANG) {
            override fun parseContentsLight(chameleon: ASTNode): FlyweightCapableTreeStructure<LighterASTNode> {
                val psi = chameleon.psi ?: error("Bad chameleon: $chameleon")
                val project = psi.project
                val factory = PsiBuilderFactory.getInstance()
                val builder = factory.createBuilder(project, chameleon)
                val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language) ?: error(this)
                val parser = ModParser()
                parser.parseLight(this, builder)
                return builder.lightTree
            }
        }

        @JvmField
        val KEY_VALUE_PAIR = Mod2EntryStubElementType()

        @JvmField
        val MAPPING = Mod2ListStubElementType()

        @JvmField
        val DOCUMENT = Mod2DocumentStubElementType()
    }
}