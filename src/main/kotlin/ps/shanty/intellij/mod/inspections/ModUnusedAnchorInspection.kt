package ps.shanty.intellij.mod.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.annotations.Nls
import org.jetbrains.yaml.YAMLBundle
import org.jetbrains.yaml.YAMLElementTypes
import org.jetbrains.yaml.psi.YAMLAnchor
import org.jetbrains.yaml.psi.YamlPsiElementVisitor
import ps.shanty.intellij.mod.ModBundle

class ModUnusedAnchorInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : YamlPsiElementVisitor() {
            override fun visitAnchor(anchor: YAMLAnchor) {
                val references =
                    ReferencesSearch.search(anchor, GlobalSearchScope.fileScope(anchor.containingFile)).findAll()
                if (references.isEmpty()) {
                    holder.registerProblem(
                        anchor,
                        ModBundle.message("inspections.unused.anchor.message", anchor.name),
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        RemoveAnchorQuickFix(anchor)
                    )
                }
            }
        }
    }

    private class RemoveAnchorQuickFix internal constructor(anchor: YAMLAnchor) : LocalQuickFix {
        private val myAnchorHolder: SmartPsiElementPointer<YAMLAnchor>

        init {
            myAnchorHolder = SmartPointerManager.getInstance(anchor.project).createSmartPsiElementPointer(anchor)
        }

        override fun getFamilyName(): @Nls String {
            return ModBundle.message("inspections.unused.anchor.quickfix.name")
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val anchor = myAnchorHolder.element ?: return
            PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                var node =
                    TreeUtil.prevLeaf(anchor.node)
                while (YAMLElementTypes.SPACE_ELEMENTS.contains(PsiUtilCore.getElementType(node))) {
                    assert(node != null)
                    val prev = TreeUtil.prevLeaf(node)
                    val parent = node!!.treeParent
                    if (parent != null) {
                        CodeEditUtil.removeChild(parent, node)
                    }
                    node = prev
                }
                anchor.delete()
            }
        }
    }
}