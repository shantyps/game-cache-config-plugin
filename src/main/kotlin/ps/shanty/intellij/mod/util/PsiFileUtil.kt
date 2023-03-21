package ps.shanty.intellij.mod.util

import com.intellij.psi.PsiFile
import ps.shanty.intellij.mod.ModFileExtension

object PsiFileUtil {
    val PsiFile.extension
        get() = ModFileExtension.byExtensionName(name.substringAfterLast("."))
}