package ps.shanty.intellij.snt

import ps.shanty.intellij.snt.psi.SNTEntry

interface DuplicateSNTEntryKeyAnnotationSuppressor {
    fun suppressAnnotationFor(sntEntry: SNTEntry): Boolean
}