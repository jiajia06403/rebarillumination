package io.github.rebarillumination

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.rebarillumination.block.*
import org.bukkit.Material
import org.bukkit.NamespacedKey

object RebarIlluminationBlocks {

    private val colors = listOf(
        "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
    )

    fun initialize() {
        // 灯：彩色玻璃基底，海晶灯/下界合金块作为展示实体
        colors.forEach { color ->
            val key = NamespacedKey(RebarIlluminationAddon.instance, "lamp_$color")
            val glassMaterial = when (color) {
                "white" -> Material.WHITE_STAINED_GLASS
                "orange" -> Material.ORANGE_STAINED_GLASS
                "magenta" -> Material.MAGENTA_STAINED_GLASS
                "light_blue" -> Material.LIGHT_BLUE_STAINED_GLASS
                "yellow" -> Material.YELLOW_STAINED_GLASS
                "lime" -> Material.LIME_STAINED_GLASS
                "pink" -> Material.PINK_STAINED_GLASS
                "gray" -> Material.GRAY_STAINED_GLASS
                "light_gray" -> Material.LIGHT_GRAY_STAINED_GLASS
                "cyan" -> Material.CYAN_STAINED_GLASS
                "purple" -> Material.PURPLE_STAINED_GLASS
                "blue" -> Material.BLUE_STAINED_GLASS
                "brown" -> Material.BROWN_STAINED_GLASS
                "green" -> Material.GREEN_STAINED_GLASS
                "red" -> Material.RED_STAINED_GLASS
                "black" -> Material.BLACK_STAINED_GLASS
                else -> Material.WHITE_STAINED_GLASS
            }
            RebarBlock.register(key, glassMaterial, LampBlock::class.java)
        }

        // 柱状灯：结构空位基底，带6个方向的柱式灯
        colors.forEach { color ->
            val key = NamespacedKey(RebarIlluminationAddon.instance, "pillar_lamp_$color")
            RebarBlock.register(key, Material.STRUCTURE_VOID, PillarLampBlock::class.java)
        }

        // 球形灯：结构空位基底，带6个方向的球形灯
        colors.forEach { color ->
            val key = NamespacedKey(RebarIlluminationAddon.instance, "sphere_lamp_$color")
            RebarBlock.register(key, Material.STRUCTURE_VOID, SphereLampBlock::class.java)
        }

        // 壁灯：结构空位基底
        colors.forEach { color ->
            val key = NamespacedKey(RebarIlluminationAddon.instance, "wall_lamp_$color")
            RebarBlock.register(key, Material.STRUCTURE_VOID, WallLampBlock::class.java)
        }

        // 条状灯：结构空位基底，东西南北方向保持竖直柱体，上下方向支持两种柱体朝向
        colors.forEach { color ->
            val key = NamespacedKey(RebarIlluminationAddon.instance, "bar_lamp_$color")
            RebarBlock.register(key, Material.STRUCTURE_VOID, BarLampBlock::class.java)
        }
    }
}
