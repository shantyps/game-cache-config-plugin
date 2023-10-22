package ps.shanty.intellij.snt

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.Processor
import ps.shanty.intellij.snt.psi.SNTEntry
import ps.shanty.intellij.snt.psi.SNTFile

class SNTKeyIndex : StringStubIndexExtension<SNTEntry>() {
    override fun getKey(): StubIndexKey<String, SNTEntry> {
        return KEY
    }

    override fun get(key: String, project: Project, scope: GlobalSearchScope): Collection<SNTEntry> {
        return StubIndex.getElements(getKey(), key, project, scope, SNTEntry::class.java)
    }

    fun processAllKeysInSNT(project: Project, sntName: String, processor: Processor<in String>): Boolean {
        val virtualFile = FilenameIndex.getVirtualFilesByName("$sntName.snt", GlobalSearchScope.allScope(project)).firstOrNull() ?: return false
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return false
        val sntFile = psiFile as? SNTFile ?: return false
        for (property in sntFile.properties) {
            if (!processor.process(property.key)) {
                break
            }
        }
        return true
    }

    companion object {
        val KEY = StubIndexKey.createIndexKey<String, SNTEntry>("snt.key.index")
        val instance = SNTKeyIndex()
    }
}