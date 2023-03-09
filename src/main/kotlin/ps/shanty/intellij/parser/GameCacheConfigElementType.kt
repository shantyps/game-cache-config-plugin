package ps.shanty.intellij.parser

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls
import org.jetbrains.yaml.YAMLFileType

class GameCacheConfigElementType(debugName: String) : IElementType(debugName, GameCacheConfigLanguage.INSTANCE)