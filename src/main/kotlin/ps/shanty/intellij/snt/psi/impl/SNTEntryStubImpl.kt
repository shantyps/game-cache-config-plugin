package ps.shanty.intellij.snt.psi.impl

import com.intellij.lang.properties.psi.Property
import com.intellij.lang.properties.psi.PropertyStub
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import ps.shanty.intellij.snt.SNTElementTypes

class SNTEntryStubImpl(parent: StubElement<*>?, private val myKey: String) : StubBase<Property>(parent, SNTElementTypes.PROPERTY), PropertyStub {
    override fun getKey(): String {
        return myKey
    }
}