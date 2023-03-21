package ps.shanty.intellij.mod

enum class ModFileExtension(val extension: String, val patchFolder: String, val type: String) {
    MOD("mod", "", ""),
    BAS(extension = "bas", patchFolder = "bas", type = ""),
    ENUM(extension = "enum", patchFolder = "enum", type = "enum"),
    HUNT(extension = "hunt", patchFolder = "hunt", type = ""),
    INV(extension = "inv", patchFolder = "inv", type = "inv"),
    LOC(extension = "loc", patchFolder = "loc", type = "loc"),
    MAPAREA(extension = "maparea", patchFolder = "map_area", type = "map_area"),
    MAPLABEL(extension = "maplabel", patchFolder = "map_label", type = ""),
    MAPFUNC(extension = "mapfunc", patchFolder = "mapfunction", type = ""),
    NPC(extension = "npc", patchFolder = "npc", type = "npc"),
    NS(extension = "ns", patchFolder = "npcspawn", type = ""),
    OBJ(extension = "obj", patchFolder = "obj", type = "named_obj"),
    PARAM(extension = "param", patchFolder = "param", type = "param"),
    SEQ(extension = "seq", patchFolder = "seq", type = "seq"),
    STRUCT(extension = "struct", patchFolder = "struct", type = "struct"),
    VARBIT(extension = "varbit", patchFolder = "varbit", type = "varbit"),
    VARCLAN(extension = "varclan", patchFolder = "varclan", type = ""),
    VARCLANSETTING(extension = "varclansetting", patchFolder = "varclansetting", type = ""),
    VARCLIENT(extension = "varclient", patchFolder = "varclient", type = ""),
    VARDOUBLE(extension = "vardouble", patchFolder = "vardouble", type = ""),
    VARLONG(extension = "varlong", patchFolder = "varlong", type = ""),
    VARP(extension = "varp", patchFolder = "varp", type = "varp"),
    VARSTRING(extension = "varstring", patchFolder = "varstring", type = ""),

    ;

    companion object {
        fun byExtensionName(extensionName: String): ModFileExtension {
            return values().first { it.extension.equals(extensionName, ignoreCase = true) }
        }

        fun byType(type: String): ModFileExtension {
            return values().first { it.type.equals(type, ignoreCase = true) }
        }
    }
}