// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package ps.shanty.intellij.mod.formatter

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import ps.shanty.intellij.mod.ModLanguage

class ModCodeStyleSettings(container: CodeStyleSettings?) :
    CustomCodeStyleSettings(ModLanguage.INSTANCE.id, container) {
    var ALIGN_VALUES_PROPERTIES = DO_NOT_ALIGN
    var INDENT_SEQUENCE_VALUE = true
    var SEQUENCE_ON_NEW_LINE = false
    var BLOCK_MAPPING_ON_NEW_LINE = false
    var SPACE_BEFORE_COLON = false

    /** Whether editor should automatically insert hyphen on Enter for subsequent (non-first) items  */
    var AUTOINSERT_SEQUENCE_MARKER = true

    companion object {
        const val DO_NOT_ALIGN = 0
        const val ALIGN_ON_VALUE = 1
        const val ALIGN_ON_COLON = 2
    }
}