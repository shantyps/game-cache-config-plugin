package ps.shanty.intellij.mod

enum class ModFileExtension(val extensions: List<String>, val patchFolder: String, val types: List<String>, val sntName: String) {
    MOD(extensions = listOf("mod", "mod2"), "", listOf(""), ""),
    BAS(extensions = listOf("bas", "bas2"), patchFolder = "bas", types = listOf(""), sntName = "bas"),
    ENUM(extensions = listOf("enum", "enum2"), patchFolder = "enum", types = listOf("enum"), sntName = "enum"),
    HUNT(extensions = listOf("hunt", "hunt2"), patchFolder = "hunt", types = listOf(""), sntName = "hunt"),
    INV(extensions = listOf("inv", "inv2"), patchFolder = "inv", types = listOf("inv"), sntName = "inv"),
    LOC(extensions = listOf("loc", "loc2"), patchFolder = "loc", types = listOf("loc"), sntName = "loc"),
    MAPAREA(extensions = listOf("maparea", "maparea2"), patchFolder = "map_area", types = listOf("map_area"), sntName = "map_area"),
    MAPLABEL(extensions = listOf("maplabel", "maplabel2"), patchFolder = "map_label", types = listOf(""), sntName = "mapfunction"),
    MAPFUNC(extensions = listOf("mel", "mel2"), patchFolder = "mapfunction", types = listOf(""), sntName = "mel"),
    NPC(extensions = listOf("npc", "npc2"), patchFolder = "npc", types = listOf("npc"), sntName = "npc"),
    NS(extensions = listOf("ns"), patchFolder = "npcspawn", types = listOf(""), sntName = ""),
    OBJ(extensions = listOf("obj", "obj2"), patchFolder = "obj", types = listOf("obj", "named_obj"), sntName = "obj"),
    PARAM(extensions = listOf("param", "param2"), patchFolder = "param", types = listOf("param"), sntName = "param"),
    SEQ(extensions = listOf("seq", "seq2"), patchFolder = "seq", types = listOf("seq"), sntName = "seq"),
    STRUCT(extensions = listOf("struct", "struct2"), patchFolder = "struct", types = listOf("struct"), sntName = "struct"),
    VARBIT(extensions = listOf("varbit", "varbit2"), patchFolder = "varbit", types = listOf("varbit"), sntName = "varbit"),
    VARCLIENT(extensions = listOf("varclient", "varclient2"), patchFolder = "varclient", types = listOf(""), sntName = "varclient"),
    VARDOUBLE(extensions = listOf("vardouble", "vardouble2"), patchFolder = "vardouble", types = listOf(""), sntName = "vardouble"),
    VARLONG(extensions = listOf("varlong", "varlong2"), patchFolder = "varlong", types = listOf(""), sntName = "varlong"),
    VARP(extensions = listOf("varp", "varp2"), patchFolder = "varp", types = listOf("varp"), sntName = "varp"),
    VARSTRING(extensions = listOf("varstring", "varstring2"), patchFolder = "varstring", types = listOf(""), sntName = "varstring"),

    ;

    companion object {
        fun byExtensionName(extensionName: String): ModFileExtension {
            return values().first { it.extensions.any { it.equals(extensionName, ignoreCase = true) } }
        }

        fun byType(type: String): ModFileExtension? {
            return values().firstOrNull { it.types.any { it.equals(type, ignoreCase = true) } }
        }
    }
}