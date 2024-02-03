package ps.shanty.intellij.mod.psi

import com.intellij.lang.ASTNode
import org.jetbrains.yaml.psi.impl.YAMLKeyValueImpl

abstract class ModKeyValue(node: ASTNode) : YAMLKeyValueImpl(node), Mod2Entry