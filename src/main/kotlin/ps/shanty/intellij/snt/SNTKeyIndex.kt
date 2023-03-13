package ps.shanty.intellij.snt

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import ps.shanty.intellij.snt.psi.SNTEntry

class SNTKeyIndex : StringStubIndexExtension<SNTEntry>() {
    override fun getKey(): StubIndexKey<String, SNTEntry> {
        return KEY
    }

    override fun get(key: String, project: Project, scope: GlobalSearchScope): Collection<SNTEntry> {
        return StubIndex.getElements(getKey(), key, project, scope, SNTEntry::class.java)
    }

    companion object {
        val KEY = StubIndexKey.createIndexKey<String, SNTEntry>("snt.key.index")
        val instance = SNTKeyIndex()
    }
}