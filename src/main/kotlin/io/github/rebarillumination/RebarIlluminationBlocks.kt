package io.github.rebarillumination

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.rebarillumination.block.*
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material
import org.bukkit.NamespacedKey

object RebarIlluminationBlocks {

    fun initialize() {
        // 注册所有灯方块
        LampColor.entries.forEach { color ->
            val colorName = color.name.lowercase()

            // 灯：使用对应颜色的染色玻璃
            val lampKey = NamespacedKey(RebarIlluminationAddon.instance, "lamp_$colorName")
            RebarBlock.register(lampKey, color.stainedGlassMaterial, LampBlock::class.java)

            // 柱状灯
            val pillarLampKey = NamespacedKey(RebarIlluminationAddon.instance, "pillar_lamp_$colorName")
            RebarBlock.register(pillarLampKey, Material.STRUCTURE_VOID, PillarLampBlock::class.java)

            // 球形灯
            val sphereLampKey = NamespacedKey(RebarIlluminationAddon.instance, "sphere_lamp_$colorName")
            RebarBlock.register(sphereLampKey, Material.STRUCTURE_VOID, SphereLampBlock::class.java)

            // 贴片灯
            val patchLampKey = NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_$colorName")
            RebarBlock.register(patchLampKey, Material.STRUCTURE_VOID, PatchLampBlock::class.java)

            // 条状灯
            val barLampKey = NamespacedKey(RebarIlluminationAddon.instance, "bar_lamp_$colorName")
            RebarBlock.register(barLampKey, Material.STRUCTURE_VOID, BarLampBlock::class.java)
        }
    }
}
