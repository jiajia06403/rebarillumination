package io.github.rebarillumination.block

import io.github.pylonmc.rebar.block.context.BlockCreateContext
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder
import io.github.rebarillumination.LampShape
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

/**
 * 普通灯：方块本身即为染色玻璃（使用真实方块材质），无需贴片展示实体。
 * 仅叠加一个略小的光源实体；彩虹模式下直接改变方块类型。
 */
class LampBlock(
    block: Block,
    context: BlockCreateContext
) : AbstractLampBlock(block, context, LampShape.LAMP) {

    override val usesBlockTexture: Boolean = true
    override val primaryEntityName: String = "light"
    override val fallbackDropMaterial: Material
        get() = if (isRainbow) Material.WHITE_STAINED_GLASS else lampColor.stainedGlassMaterial

    // 方块灯是实心染色玻璃，粒子锚点在方块中心（会被方块遮挡），故放大扩散半径与数量，
    // 让粒子逸出方块边界而可见；中心仍是方块中心，不抬到方块上方。
    override val faultParticleSpread: Double = 0.8
    override val faultParticleCount: Int = 12

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : this(
        block,
        BlockCreateContext.Default(block = block, facing = BlockFace.UP, facingVertical = BlockFace.UP)
    ) {
        readState(pdc)
    }

    override fun spawnDisplayEntities() {
        // 基础方块已是彩色玻璃，仅叠加一个略小的光源展示实体（防止 z-fighting）。
        addEntity("light", ItemDisplayBuilder()
            .itemStack(ItemStack(if (isLit) Material.SEA_LANTERN else Material.NETHERITE_BLOCK))
            .transformation(TransformBuilder().scale(0.999f).buildForItemDisplay())
            .build(getDisplayEntityLocation()))
    }

    override fun updateRainbowDisplay(color: LampColor) {
        block.type = color.stainedGlassMaterial
    }
}
