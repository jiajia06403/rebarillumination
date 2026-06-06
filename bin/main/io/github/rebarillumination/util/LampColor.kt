package io.github.rebarillumination.util

import org.bukkit.Material

enum class LampColor(
    val dyeMaterial: Material,
    val stainedGlassMaterial: Material
) {
    WHITE(Material.WHITE_DYE, Material.WHITE_STAINED_GLASS),
    ORANGE(Material.ORANGE_DYE, Material.ORANGE_STAINED_GLASS),
    MAGENTA(Material.MAGENTA_DYE, Material.MAGENTA_STAINED_GLASS),
    LIGHT_BLUE(Material.LIGHT_BLUE_DYE, Material.LIGHT_BLUE_STAINED_GLASS),
    YELLOW(Material.YELLOW_DYE, Material.YELLOW_STAINED_GLASS),
    LIME(Material.LIME_DYE, Material.LIME_STAINED_GLASS),
    PINK(Material.PINK_DYE, Material.PINK_STAINED_GLASS),
    GRAY(Material.GRAY_DYE, Material.GRAY_STAINED_GLASS),
    LIGHT_GRAY(Material.LIGHT_GRAY_DYE, Material.LIGHT_GRAY_STAINED_GLASS),
    CYAN(Material.CYAN_DYE, Material.CYAN_STAINED_GLASS),
    PURPLE(Material.PURPLE_DYE, Material.PURPLE_STAINED_GLASS),
    BLUE(Material.BLUE_DYE, Material.BLUE_STAINED_GLASS),
    BROWN(Material.BROWN_DYE, Material.BROWN_STAINED_GLASS),
    GREEN(Material.GREEN_DYE, Material.GREEN_STAINED_GLASS),
    RED(Material.RED_DYE, Material.RED_STAINED_GLASS),
    BLACK(Material.BLACK_DYE, Material.BLACK_STAINED_GLASS);

    companion object {
        fun fromName(name: String): LampColor {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: WHITE
        }
    }
}
