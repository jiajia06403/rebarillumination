package io.github.rebarillumination.block

import io.github.pylonmc.rebar.block.context.BlockCreateContext
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder
import io.github.rebarillumination.LampShape
import io.github.rebarillumination.RebarIlluminationAddon
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.joml.Matrix4f
import org.joml.Vector3f

/**
 * 条状灯：水平或竖直放置的柱状灯体，除了竖直朝向（[facing]）外，还记录一个
 * 水平朝向（[horizontalFacing]）用于竖直放置时决定灯体指向。
 */
class BarLampBlock(
    block: Block,
    context: BlockCreateContext
) : AbstractLampBlock(block, context, LampShape.BAR) {

    override val displayOffset: Double = 0.49

    /** 水平朝向，仅在竖直放置时使用，决定灯体指向。 */
    var horizontalFacing: BlockFace = context.facing

    companion object {
        private val HORIZONTAL_FACING_KEY = NamespacedKey(RebarIlluminationAddon.instance, "lamp_facing")
    }

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : this(
        block,
        BlockCreateContext.Default(block = block, facing = BlockFace.UP, facingVertical = BlockFace.UP)
    ) {
        readState(pdc)
    }

    override fun readPersistentData(pdc: PersistentDataContainer) {
        horizontalFacing = pdc.get(HORIZONTAL_FACING_KEY, PersistentDataType.STRING)
            ?.let { BlockFace.valueOf(it) } ?: BlockFace.NORTH
    }

    override fun writePersistentData(pdc: PersistentDataContainer) {
        pdc.set(HORIZONTAL_FACING_KEY, PersistentDataType.STRING, horizontalFacing.name)
    }

    override fun spawnDisplayEntities() {
        val loc = getDisplayEntityLocation()
        val (shellTransform, lightTransform) = when (facing) {
            BlockFace.NORTH -> createHorizontalTransform(0.0, 0.0)
            BlockFace.SOUTH -> createHorizontalTransform(0.0, Math.PI)
            BlockFace.EAST -> createHorizontalTransform(0.0, Math.PI / 2)
            BlockFace.WEST -> createHorizontalTransform(0.0, -Math.PI / 2)
            BlockFace.UP, BlockFace.DOWN -> createVerticalTransform(horizontalFacing)
            else -> createHorizontalTransform(0.0, 0.0)
        }

        addEntity("shell", ItemDisplayBuilder()
            .itemStack(ItemStack(lampColor.stainedGlassMaterial))
            .transformation(shellTransform)
            .build(loc))

        addEntity("light", ItemDisplayBuilder()
            .itemStack(ItemStack(if (isLit) Material.SEA_LANTERN else Material.NETHERITE_BLOCK))
            .transformation(lightTransform)
            .build(loc))
    }

    /** 水平放置（NORTH/SOUTH/EAST/WEST）：柱体竖直，以 yaw 旋转控制水平朝向。 */
    private fun createHorizontalTransform(pitch: Double, yaw: Double): Pair<Matrix4f, Matrix4f> {
        val shellTransform = TransformBuilder()
            .rotate(pitch, yaw, 0.0)
            .scale(0.201f, 0.801f, 0.201f)
            .buildForItemDisplay()

        val lightTransform = TransformBuilder()
            .rotate(pitch, yaw, 0.0)
            .scale(0.12f, 0.6f, 0.12f)
            .buildForItemDisplay()

        return Pair(shellTransform, lightTransform)
    }

    /** 竖直放置（UP/DOWN）：柱体水平，使用 lookAlong 指向 [horizontalFacing] 方向。 */
    private fun createVerticalTransform(horizontalFacing: BlockFace): Pair<Matrix4f, Matrix4f> {
        val direction = when (horizontalFacing) {
            BlockFace.NORTH -> Vector3f(0f, 0f, 1f)
            BlockFace.SOUTH -> Vector3f(0f, 0f, -1f)
            BlockFace.EAST -> Vector3f(-1f, 0f, 0f)
            BlockFace.WEST -> Vector3f(1f, 0f, 0f)
            else -> Vector3f(0f, 0f, 1f)
        }

        val shellTransform = TransformBuilder()
            .lookAlong(direction)
            .scale(0.201f, 0.201f, 0.801f)
            .buildForItemDisplay()

        val lightTransform = TransformBuilder()
            .lookAlong(direction)
            .scale(0.12f, 0.12f, 0.6f)
            .buildForItemDisplay()

        return Pair(shellTransform, lightTransform)
    }

    override fun updateRainbowDisplay(color: LampColor) {
        val shellDisplay = getHeldEntity(ItemDisplay::class.java, "shell")
        shellDisplay?.setItemStack(ItemStack(color.stainedGlassMaterial))
    }
}
