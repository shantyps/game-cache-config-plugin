package ps.shanty.intellij.snt.psi

import com.intellij.lang.properties.psi.PropertyStub
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.StubBasedPsiElement
import ps.shanty.intellij.snt.ISNTEntry

interface SNTEntry : PsiNamedElement, StubBasedPsiElement<PropertyStub?>, NavigatablePsiElement, ISNTEntry