package io.github.rebarillumination.block

import io.github.pylonmc.rebar.block.context.BlockCreateContext
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder
import io.github.rebarillumination.LampShape
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

/**
 * 球形灯：球体外观，外壳 + 光源核心两层展示实体。
 */
class SphereLampBlock(
    block: Block,
    context: BlockCreateContext
) : AbstractLampBlock(block, context, LampShape.SPHERE) {

    override val displayOffset: Double = 0.3

    // 还原重构前球形灯的开/关音效表现
    override val toggleSoundVolume: Float = 0.9f
    override val toggleSoundPitch: Float = 0.9f

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : this(
        block,
        BlockCreateContext.Default(block = block, facing = BlockFace.UP, facingVertical = BlockFace.UP)
    ) {
        readState(pdc)
    }

    override fun spawnDisplayEntities() {
        val loc = getDisplayEntityLocation()

        val shellTransform = TransformBuilder()
            .lookAlong(facing)
            .scale(0.501f)
            .buildForItemDisplay()

        addEntity("shell", ItemDisplayBuilder()
            .itemStack(ItemStack(lampColor.stainedGlassMaterial))
            .transformation(shellTransform)
            .build(loc))

        val lightTransform = TransformBuilder()
            .lookAlong(facing)
            .scale(0.3f)
            .buildForItemDisplay()

        addEntity("light", ItemDisplayBuilder()
            .itemStack(ItemStack(if (isLit) Material.SEA_LANTERN else Material.NETHERITE_BLOCK))
            .transformation(lightTransform)
            .build(loc))
    }

    override fun updateRainbowDisplay(color: LampColor) {
        val shellDisplay = getHeldEntity(ItemDisplay::class.java, "shell")
        shellDisplay?.setItemStack(ItemStack(color.stainedGlassMaterial))
    }
}
