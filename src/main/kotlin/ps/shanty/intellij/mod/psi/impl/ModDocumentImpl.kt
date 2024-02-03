package ps.shanty.intellij.mod.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLValue
import ps.shanty.intellij.mod.Mod2ElementTypes
import ps.shanty.intellij.mod.psi.Mod2Document
import ps.shanty.intellij.mod.psi.Mod2DocumentStub

class ModDocumentImpl : StubBasedPsiElementBase<Mod2DocumentStub>, Mod2Document, YAMLDocument {

    constructor(node: ASTNode) : super(node)

    constructor(stub: Mod2DocumentStub) : super(stub, Mod2ElementTypes.MAPPING)

    override fun getTopLevelValue(): YAMLValue? {
        return PsiTreeUtil.findChildOfType(this, YAMLValue::class.java)
    }

    override fun toString(): String {
        return "MOD Document"
    }
}