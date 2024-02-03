package ps.shanty.intellij.mod

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import ps.shanty.intellij.mod.psi.Mod2Entry

class Mod2KeyIndex : StringStubIndexExtension<Mod2Entry>() {
    override fun getKey(): StubIndexKey<String, Mod2Entry> {
        return KEY
    }

    override fun get(key: String, project: Project, scope: GlobalSearchScope): Collection<Mod2Entry> {
        return StubIndex.getElements(getKey(), key, project, scope, Mod2Entry::class.java)
    }

    companion object {
        val KEY = StubIndexKey.createIndexKey<String, Mod2Entry>("mod2.key.index")
        val instance = Mod2KeyIndex()
    }
}