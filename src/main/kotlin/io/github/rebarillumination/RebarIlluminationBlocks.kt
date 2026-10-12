package io.github.rebarillumination

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.rebarillumination.block.AbstractLampBlock
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material

object RebarIlluminationBlocks {

    fun initialize() {
        // 遍历所有颜色 × 所有形状，集中登记方块。
        // 形状对应的 key 前缀、方块类、是否使用真实染色玻璃都在 LampShape 中登记，
        // 新增形状无需改动此处。
        LampColor.entries.forEach { color ->
            LampShape.entries.forEach { shape ->
                val material = if (shape.usesDirectStainedGlass) {
                    color.stainedGlassMaterial
                } else {
                    Material.STRUCTURE_VOID
                }
                RebarBlock.register(shape.blockKey(color), material, shape.blockClass.java)
            }
        }
    }
}
