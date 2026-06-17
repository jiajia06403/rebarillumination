package io.github.rebarillumination.block

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.block.interfaces.BlockBreakRebarBlockHandler
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock
import io.github.pylonmc.rebar.block.interfaces.EntityHolderRebarBlock
import io.github.pylonmc.rebar.block.interfaces.InteractRebarBlockHandler
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock
import io.github.pylonmc.rebar.block.context.BlockCreateContext
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler
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
import io.github.pylonmc.rebar.util.delayTicks
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

class SphereLampBlock(
    block: Block,
    context: BlockCreateContext
) : RebarBlock(block, context), InteractRebarBlockHandler, BlockBreakRebarBlockHandler, EntityHolderRebarBlock, DirectionalRebarBlock, TickingRebarBlock, FaultyLamp, RainbowLamp {

    companion object {
        private val IS_LIT_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "is_lit")
        private val FACING_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "facing")
    }

    override val isLit: Boolean
        get() = _isLit
    private var _isLit: Boolean = true
    override var disableBlockTextureEntity = true
    override var facing: BlockFace = context.facingVertical
    override var isFaulty: Boolean = false
    override var faultyEndTick: Long = 0L
    override var lampColor: LampColor = LampColor.WHITE
    override var isRainbow: Boolean = false
    override var lastRainbowColorIndex: Int = -1
    
    override var rainbowJob: kotlinx.coroutines.Job? = null

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : this(
        block,
        BlockCreateContext.Default(block = block, facing = BlockFace.UP, facingVertical = BlockFace.UP)
    ) {
        _isLit = pdc.get(IS_LIT_KEY, PersistentDataType.BOOLEAN) ?: true
        facing = pdc.get(FACING_KEY, PersistentDataType.STRING)?.let { BlockFace.valueOf(it) } ?: BlockFace.UP
        isFaulty = pdc.get(IS_FAULTY_KEY, PersistentDataType.BOOLEAN) ?: false
        faultyEndTick = pdc.get(FAULTY_TICK_KEY, PersistentDataType.LONG) ?: 0L
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.set(IS_LIT_KEY, PersistentDataType.BOOLEAN, isLit)
        pdc.set(FACING_KEY, PersistentDataType.STRING, facing.name)
        pdc.set(IS_FAULTY_KEY, PersistentDataType.BOOLEAN, isFaulty)
        pdc.set(FAULTY_TICK_KEY, PersistentDataType.LONG, faultyEndTick)
    }

    init {
        val keyStr = key.key
        val colorName = keyStr.removePrefix("sphere_lamp_")
        lampColor = LampColor.fromName(colorName)
        isRainbow = colorName == "rainbow"
    }

    override fun postInitialise() {
        // 只在实体不存在时创建，避免加载时重复创建
        if (!isHeldEntityPresent("shell")) {
            createDisplayEntities()
        }
        if (isRainbow) {
            startRainbowTask()
        }
    }

    /**
     * 创建展示实体（只在 create constructor 中调用）
     */
    private fun createDisplayEntities() {
        disableBlockTextureEntity = true

        val blockFacing = this.facing

        // 根据朝向计算偏移
        var offsetX = 0.0
        var offsetY = 0.0
        var offsetZ = 0.0

        when (blockFacing) {
            BlockFace.UP -> offsetY = -0.3
            BlockFace.DOWN -> offsetY = 0.3
            BlockFace.NORTH -> offsetZ = 0.3
            BlockFace.SOUTH -> offsetZ = -0.3
            BlockFace.EAST -> offsetX = -0.3
            BlockFace.WEST -> offsetX = 0.3
            else -> offsetY = 0.0
        }

        val centerLoc = block.location.toCenterLocation().add(offsetX, offsetY, offsetZ)

        // 球形灯外壳 - 使用lookAlong朝向正确方向
        addEntity("shell", ItemDisplayBuilder()
            .itemStack(ItemStack(lampColor.stainedGlassMaterial))
            .transformation(TransformBuilder()
                .lookAlong(blockFacing)
                .scale(0.501f)
                .buildForItemDisplay())
            .build(centerLoc))

        // 光源核心
        addEntity("light", ItemDisplayBuilder()
            .itemStack(ItemStack(if (isLit) Material.SEA_LANTERN else Material.NETHERITE_BLOCK))
            .transformation(TransformBuilder()
                .lookAlong(blockFacing)
                .scale(0.3f)
                .buildForItemDisplay())
            .build(centerLoc))
    }

    override fun postLoad() {
        disableBlockTextureEntity = true
        updateDisplayEntities()
        if (isRainbow) {
            updateRainbowDisplay(LampColor.WHITE)
        }
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

    override fun updateRainbowDisplay(color: LampColor) {
        val shellDisplay = getHeldEntity(ItemDisplay::class.java, "shell")
        shellDisplay?.setItemStack(ItemStack(color.stainedGlassMaterial))
    }

    /**
     * 获取展示实体的位置
     */
    override fun getDisplayEntityLocation(): org.bukkit.Location {
        val blockFacing = this.facing
        var offsetX = 0.0
        var offsetY = 0.0
        var offsetZ = 0.0

        when (blockFacing) {
            BlockFace.UP -> offsetY = -0.3
            BlockFace.DOWN -> offsetY = 0.3
            BlockFace.NORTH -> offsetZ = 0.3
            BlockFace.SOUTH -> offsetZ = -0.3
            BlockFace.EAST -> offsetX = -0.3
            BlockFace.WEST -> offsetX = 0.3
            else -> offsetY = 0.0
        }

        return block.location.toCenterLocation().add(offsetX, offsetY, offsetZ)
    }

    @MultiHandler(priorities = [EventPriority.NORMAL, EventPriority.MONITOR])
    override fun onInteractedWith(event: PlayerInteractEvent, priority: EventPriority) {
        if (!event.action.isRightClick
            || event.hand != EquipmentSlot.HAND
            || event.useInteractedBlock() == Event.Result.DENY) {
            return
        }

        // 如果在故障模式下
        if (isFaulty) {
            if (event.player.isSneaking) {
                // 故障模式下禁止切换灯
                if (priority == EventPriority.NORMAL) {
                    event.setUseItemInHand(Event.Result.DENY)
                }
                return
            }
            // 尝试右键修复
            if (tryRightClickFix(event, priority)) {
                return
            }
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

        // 获取展示实体的位置
        val entityLocation = getDisplayEntityLocation()

        // 根据开关状态播放对应音效
        val sound = if (previousLit) Sound.BLOCK_STONE_BUTTON_CLICK_OFF else Sound.BLOCK_STONE_BUTTON_CLICK_ON
        player.playSound(entityLocation, sound, 0.9f, 0.9f)
    }

    override fun onPreBlockBreak(context: io.github.pylonmc.rebar.block.context.BlockBreakContext): Boolean {
        stopRainbowTask()
        tryRemoveAllEntities()
        return true
    }

    override fun getDropItem(context: io.github.pylonmc.rebar.block.context.BlockBreakContext): ItemStack? {
        val itemStack = defaultItem?.getItemStack()
        return itemStack ?: ItemStack(Material.STRUCTURE_VOID)
    }
    
    override fun tick() {
        tickFaultyMode()
    }
    
    override fun getWaila(player: Player): WailaDisplay? {
        val display = WailaDisplay.of(this, player)
        val status = when {
            isFaulty -> Component.translatable("rebarillumination.message.lamp.faulty_mode")
            isLit -> Component.translatable("rebarillumination.message.lamp.state_on")
            else -> Component.translatable("rebarillumination.message.lamp.state_off")
        }
        display.add(status)
        return display
    }
}
