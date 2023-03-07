package ps.shanty.intellij.data

import com.intellij.psi.PsiElement

class GameCacheConfigNames {

    var configElements = mapOf<String, List<PsiElement>>()

    companion object {
        val INSTANCE = GameCacheConfigNames()
    }
}