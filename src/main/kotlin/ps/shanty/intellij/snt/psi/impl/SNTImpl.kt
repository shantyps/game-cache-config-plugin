package ps.shanty.intellij.snt.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.properties.psi.impl.PropertiesStubElementImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import ps.shanty.intellij.snt.SNTElementTypes
import ps.shanty.intellij.snt.psi.SNT
import ps.shanty.intellij.snt.psi.SNTEntry
import ps.shanty.intellij.snt.psi.SNTStub
import java.util.function.Predicate
import java.util.stream.Collectors

class SNTImpl : PropertiesStubElementImpl<SNTStub?>, SNT {
    constructor(node: ASTNode?) : super(node)
    constructor(stub: SNTStub?) : super(stub, SNTElementTypes.PROPERTIES_LIST)

    override fun toString(): String {
        return "SNT"
    }

    override val docCommentText: String
        get() {
            val firstProp = PsiTreeUtil.getChildOfType(
                this,
                SNTEntry::class.java
            ) ?: return text

            // If there are no properties in the property file,
            // then the whole content of the file is considered to be a doc comment
            val upperEdge = SNTEntryImpl.getEdgeOfProperty(firstProp)
            val comments = PsiTreeUtil.getChildrenOfTypeAsList(
                this,
                PsiElement::class.java
            )
            return comments.stream()
                .takeWhile(Predicate.not { obj: PsiElement? ->
                    upperEdge == obj
                })
                .map { obj: PsiElement -> obj.text }
                .collect(Collectors.joining())
        }
}