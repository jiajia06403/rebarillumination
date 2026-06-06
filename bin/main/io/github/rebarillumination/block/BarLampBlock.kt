package io.github.rebarillumination.block

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.block.base.RebarBreakHandler
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock
import io.github.pylonmc.rebar.block.base.RebarEntityHolderBlock
import io.github.pylonmc.rebar.block.base.RebarInteractBlock
import io.github.pylonmc.rebar.block.base.RebarTickingBlock
import io.github.pylonmc.rebar.block.context.BlockCreateContext
import io.github.pylonmc.rebar.event.RebarBlockPlaceEvent
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder
import io.github.pylonmc.rebar.waila.WailaDisplay
import io.github.pylonmc.rebar.i18n.RebarArgument
import io.github.rebarillumination.RebarIlluminationAddon
import io.github.rebarillumination.util.LampColor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.joml.Matrix4f
import org.joml.Vector3f

class BarLampBlock(
    block: Block,
    context: BlockCreateContext
) : RebarBlock(block, context), RebarInteractBlock, RebarBreakHandler, RebarEntityHolderBlock, RebarDirectionalBlock, RebarTickingBlock, FaultyLamp {

    companion object {
        private val IS_LIT_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "is_lit")
        private val FACING_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "lamp_facing")
        private val HORIZONTAL_FACING_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "horizontal_facing")
    }

    override val isLit: Boolean
        get() = _isLit
    private var _isLit: Boolean = true
    override var disableBlockTextureEntity = true
    override var facing: BlockFace = context.facingVertical
    private var horizontalFacing: BlockFace = context.facing // 水平方向的朝向
    override var isFaulty: Boolean = false
    override var faultyEndTick: Long = 0L

    init {
        setTickInterval(FaultyLamp.lampFaultTickInterval)
    }

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : this(
        block,
        BlockCreateContext.Default(block = block, facing = BlockFace.NORTH, facingVertical = BlockFace.UP)
    ) {
        _isLit = pdc.get(IS_LIT_KEY, PersistentDataType.BOOLEAN) ?: true
        facing = pdc.get(FACING_KEY, PersistentDataType.STRING)?.let { BlockFace.valueOf(it) } ?: BlockFace.UP
        horizontalFacing = pdc.get(HORIZONTAL_FACING_KEY, PersistentDataType.STRING)?.let { BlockFace.valueOf(it) } ?: BlockFace.NORTH
        isFaulty = pdc.get(IS_FAULTY_KEY, PersistentDataType.BOOLEAN) ?: false
        faultyEndTick = pdc.get(FAULTY_TICK_KEY, PersistentDataType.LONG) ?: 0L
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.set(IS_LIT_KEY, PersistentDataType.BOOLEAN, _isLit)
        pdc.set(FACING_KEY, PersistentDataType.STRING, facing.name)
        pdc.set(HORIZONTAL_FACING_KEY, PersistentDataType.STRING, horizontalFacing.name)
        pdc.set(IS_FAULTY_KEY, PersistentDataType.BOOLEAN, isFaulty)
        pdc.set(FAULTY_TICK_KEY, PersistentDataType.LONG, faultyEndTick)
    }

    override var lampColor: LampColor = LampColor.WHITE
        get() {
            val keyStr = key.key
            val colorName = keyStr.removePrefix("bar_lamp_")
            return LampColor.fromName(colorName)
        }

    override fun postInitialise() {
        // 只在实体不存在时创建，避免加载时重复创建
        if (!isHeldEntityPresent("shell")) {
            createDisplayEntities()
        }
    }

    /**
     * 创建展示实体
     */
    private fun createDisplayEntities() {
        val centerLoc = block.location.toCenterLocation()
        
        // 根据朝向计算偏移和创建变换
        val (offset, shellTransform, lightTransform) = when (facing) {
            BlockFace.NORTH -> {
                val (shell, light) = createHorizontalTransform(0.0, 0.0)
                Triple(org.bukkit.util.Vector(0.0, 0.0, 0.49), shell, light)
            }
            BlockFace.SOUTH -> {
                val (shell, light) = createHorizontalTransform(0.0, Math.PI)
                Triple(org.bukkit.util.Vector(0.0, 0.0, -0.49), shell, light)
            }
            BlockFace.EAST -> {
                val (shell, light) = createHorizontalTransform(0.0, Math.PI / 2)
                Triple(org.bukkit.util.Vector(-0.49, 0.0, 0.0), shell, light)
            }
            BlockFace.WEST -> {
                val (shell, light) = createHorizontalTransform(0.0, -Math.PI / 2)
                Triple(org.bukkit.util.Vector(0.49, 0.0, 0.0), shell, light)
            }
            BlockFace.UP -> {
                val (shell, light) = createVerticalTransform(horizontalFacing)
                Triple(org.bukkit.util.Vector(0.0, -0.49, 0.0), shell, light)
            }
            BlockFace.DOWN -> {
                val (shell, light) = createVerticalTransform(horizontalFacing)
                Triple(org.bukkit.util.Vector(0.0, 0.49, 0.0), shell, light)
            }
            else -> {
                val (shell, light) = createHorizontalTransform(0.0, 0.0)
                Triple(org.bukkit.util.Vector(0.0, 0.0, 0.49), shell, light)
            }
        }

        addEntity("shell", ItemDisplayBuilder()
            .itemStack(ItemStack(lampColor.stainedGlassMaterial))
            .transformation(shellTransform)
            .build(centerLoc.clone().add(offset)))

        addEntity("light", ItemDisplayBuilder()
            .itemStack(ItemStack(if (isLit) Material.SEA_LANTERN else Material.NETHERITE_BLOCK))
            .transformation(lightTransform)
            .build(centerLoc.clone().add(offset)))
    }

    /**
     * 创建水平方向的变换（NORTH/SOUTH/EAST/WEST）
     * 柱体竖直放置，通过yaw旋转控制水平朝向
     */
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

    /**
     * 创建垂直方向的变换（UP/DOWN）
     * 柱体水平放置，使用lookAlong指定朝向方向
     */
    private fun createVerticalTransform(horizontalFacing: BlockFace): Pair<Matrix4f, Matrix4f> {
        // 根据horizontalFacing确定朝向向量
        val direction = when (horizontalFacing) {
            BlockFace.NORTH -> Vector3f(0f, 0f, 1f)
            BlockFace.SOUTH -> Vector3f(0f, 0f, -1f)
            BlockFace.EAST -> Vector3f(-1f, 0f, 0f)
            BlockFace.WEST -> Vector3f(1f, 0f, 0f)
            else -> Vector3f(0f, 0f, 1f)
        }
        
        // 使用lookAlong让柱体水平指向指定方向
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

    override fun postLoad() {
        disableBlockTextureEntity = true
        updateDisplayEntities()
    }

    override fun updateDisplayEntities() {
        val lightDisplay = getHeldEntity(ItemDisplay::class.java, "light")
        val displayMaterial = when {
            !_isLit -> Material.NETHERITE_BLOCK
            isCurrentlyFaulty() -> Material.REDSTONE_BLOCK
            else -> Material.SEA_LANTERN
        }
        lightDisplay?.setItemStack(ItemStack(displayMaterial))
    }

    @MultiHandler(priorities = [EventPriority.NORMAL, EventPriority.MONITOR])
    override fun onInteract(event: PlayerInteractEvent, priority: EventPriority) {
        if (!event.action.isRightClick
            || event.hand != EquipmentSlot.HAND
            || event.useInteractedBlock() == Event.Result.DENY) {
            return
        }

        if (event.player.isSneaking) {
            if (priority == EventPriority.NORMAL) {
                event.setUseItemInHand(Event.Result.DENY)
                return
            }
            toggleLight(event.player)
            return
        }

        handleFaultyModeInteraction(event, priority)
    }

    private fun toggleLight(player: Player) {
        val previousLit = _isLit
        _isLit = !_isLit
        updateDisplayEntities()
        
        // 根据开关状态播放对应音效
        val sound = if (previousLit) Sound.BLOCK_STONE_BUTTON_CLICK_OFF else Sound.BLOCK_STONE_BUTTON_CLICK_ON
        player.playSound(block.location, sound, 1.0f, 1.0f)
        
        player.spawnParticle(
            org.bukkit.Particle.FLAME,
            block.location.clone().add(0.5, 0.5, 0.5),
            5, 0.2, 0.2, 0.2, 0.1
        )
    }

    override fun onBreak(drops: MutableList<ItemStack>, context: io.github.pylonmc.rebar.block.context.BlockBreakContext) {
        tryRemoveAllEntities()
        val itemStack = defaultItem?.getItemStack()
        if (itemStack != null) {
            drops.add(itemStack)
        } else {
            drops.add(ItemStack(Material.STRUCTURE_VOID))
        }
    }
    
    override fun tick() {
        tickFaultyMode()
    }
    
    override fun getWaila(player: Player): WailaDisplay? {
        val status = when {
            isFaulty -> Component.translatable("rebarillumination.message.lamp.faulty_mode")
            isLit -> Component.translatable("rebarillumination.message.lamp.state_on")
            else -> Component.translatable("rebarillumination.message.lamp.state_off")
        }
        return WailaDisplay(defaultWailaTranslationKey.arguments(RebarArgument.of("status", status)))
    }
}