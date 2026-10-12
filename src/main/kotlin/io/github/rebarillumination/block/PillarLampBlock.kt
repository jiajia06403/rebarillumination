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
 * 柱状灯：长轴沿朝向的柱体，外壳 + 光源核心两层展示实体。
 */
class PillarLampBlock(
    block: Block,
    context: BlockCreateContext
) : AbstractLampBlock(block, context, LampShape.PILLAR) {

    override val displayOffset: Double = 0.2

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
        val lightTransform = TransformBuilder()

        // 根据朝向把柱体长轴旋转到目标方向，长轴始终在 Y 方向（默认竖直）。
        when (facing) {
            BlockFace.NORTH -> {
                shellTransform.rotate(Math.PI / 2, 0.0, 0.0)
                lightTransform.rotate(Math.PI / 2, 0.0, 0.0)
            }
            BlockFace.SOUTH -> {
                shellTransform.rotate(-Math.PI / 2, 0.0, 0.0)
                lightTransform.rotate(-Math.PI / 2, 0.0, 0.0)
            }
            BlockFace.EAST -> {
                shellTransform.rotate(0.0, 0.0, -Math.PI / 2)
                lightTransform.rotate(0.0, 0.0, -Math.PI / 2)
            }
            BlockFace.WEST -> {
                shellTransform.rotate(0.0, 0.0, Math.PI / 2)
                lightTransform.rotate(0.0, 0.0, Math.PI / 2)
            }
            BlockFace.DOWN -> {
                shellTransform.rotate(Math.PI, 0.0, 0.0)
                lightTransform.rotate(Math.PI, 0.0, 0.0)
            }
            else -> { /* UP：无需旋转 */ }
        }

        shellTransform.scale(0.301f, 0.801f, 0.301f)
        lightTransform.scale(0.2f, 0.6f, 0.2f)

        addEntity("shell", ItemDisplayBuilder()
            .itemStack(ItemStack(lampColor.stainedGlassMaterial))
            .transformation(shellTransform.buildForItemDisplay())
            .build(loc))

        addEntity("light", ItemDisplayBuilder()
            .itemStack(ItemStack(if (isLit) Material.SEA_LANTERN else Material.NETHERITE_BLOCK))
            .transformation(lightTransform.buildForItemDisplay())
            .build(loc))
    }

    override fun updateRainbowDisplay(color: LampColor) {
        val shellDisplay = getHeldEntity(ItemDisplay::class.java, "shell")
        shellDisplay?.setItemStack(ItemStack(color.stainedGlassMaterial))
    }
}
