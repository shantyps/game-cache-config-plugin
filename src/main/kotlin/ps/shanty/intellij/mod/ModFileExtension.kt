package ps.shanty.intellij.mod

enum class ModFileExtension(val extension: String, val patchFolder: String, val type: String, val sntName: String) {
    MOD("mod", "", "", ""),
    BAS(extension = "bas", patchFolder = "bas", type = "", sntName = "bas"),
    ENUM(extension = "enum", patchFolder = "enum", type = "enum", sntName = "enum"),
    HUNT(extension = "hunt", patchFolder = "hunt", type = "", sntName = "hunt"),
    INV(extension = "inv", patchFolder = "inv", type = "inv", sntName = "inv"),
    LOC(extension = "loc", patchFolder = "loc", type = "loc", sntName = "loc"),
    MAPAREA(extension = "maparea", patchFolder = "map_area", type = "map_area", sntName = "map_area"),
    MAPLABEL(extension = "maplabel", patchFolder = "map_label", type = "", sntName = "mapfunction"),
    MAPFUNC(extension = "mapfunc", patchFolder = "mapfunction", type = "", sntName = "mapfunction"),
    NPC(extension = "npc", patchFolder = "npc", type = "npc", sntName = "npc"),
    NS(extension = "ns", patchFolder = "npcspawn", type = "", sntName = ""),
    OBJ(extension = "obj", patchFolder = "obj", type = "named_obj", sntName = "obj"),
    PARAM(extension = "param", patchFolder = "param", type = "param", sntName = "param"),
    SEQ(extension = "seq", patchFolder = "seq", type = "seq", sntName = "seq"),
    STRUCT(extension = "struct", patchFolder = "struct", type = "struct", sntName = "struct"),
    VARBIT(extension = "varbit", patchFolder = "varbit", type = "varbit", sntName = "varbit"),
    VARCLIENT(extension = "varclient", patchFolder = "varclient", type = "", sntName = "varclient"),
    VARDOUBLE(extension = "vardouble", patchFolder = "vardouble", type = "", sntName = "vardouble"),
    VARLONG(extension = "varlong", patchFolder = "varlong", type = "", sntName = "varlong"),
    VARP(extension = "varp", patchFolder = "varp", type = "varp", sntName = "varp"),
    VARSTRING(extension = "varstring", patchFolder = "varstring", type = "", sntName = "varstring"),

    ;

    companion object {
        fun byExtensionName(extensionName: String): ModFileExtension {
            return values().first { it.extension.equals(extensionName, ignoreCase = true) }
        }

        fun byType(type: String): ModFileExtension? {
            return values().firstOrNull { it.type.equals(type, ignoreCase = true) }
        }
    }
}