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
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class LampBlock(
    block: Block,
    context: BlockCreateContext
) : RebarBlock(block, context), InteractRebarBlockHandler, BlockBreakRebarBlockHandler, EntityHolderRebarBlock, TickingRebarBlock, FaultyLamp, DirectionalRebarBlock {

    companion object {
        private val IS_LIT_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "is_lit")
        private val COLOR_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "color")
        private val FACING_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "facing")
        private val RAINBOW_INDEX_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "rainbow_index")
        private val LAST_RAINBOW_CHANGE_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "last_rainbow_change")
        private val NON_RAINBOW_COLORS = LampColor.getNonRainbowColors()
    }

    override val isLit: Boolean
        get() = _isLit
    private var _isLit: Boolean = true
    override var lampColor: LampColor = LampColor.WHITE
    override var facing: BlockFace = context.facingVertical
    override var isFaulty: Boolean = false
    override var faultyEndTick: Long = 0L
    private var isRainbow: Boolean = false
    private var rainbowColorIndex: Int = 0
    private var lastRainbowChangeTick: Long = 0L

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : this(
        block,
        BlockCreateContext.Default(block = block, facing = BlockFace.UP, facingVertical = BlockFace.UP)
    ) {
        _isLit = pdc.get(IS_LIT_KEY, PersistentDataType.BOOLEAN) ?: true
        val colorName = pdc.get(COLOR_KEY, PersistentDataType.STRING) ?: "white"
        lampColor = LampColor.fromName(colorName)
        facing = pdc.get(FACING_KEY, PersistentDataType.STRING)?.let { BlockFace.valueOf(it) } ?: BlockFace.UP
        isFaulty = pdc.get(IS_FAULTY_KEY, PersistentDataType.BOOLEAN) ?: false
        faultyEndTick = pdc.get(FAULTY_TICK_KEY, PersistentDataType.LONG) ?: 0L
        rainbowColorIndex = pdc.get(RAINBOW_INDEX_KEY, PersistentDataType.INTEGER) ?: 0
        lastRainbowChangeTick = pdc.get(LAST_RAINBOW_CHANGE_KEY, PersistentDataType.LONG) ?: 0L
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.set(IS_LIT_KEY, PersistentDataType.BOOLEAN, isLit)
        pdc.set(COLOR_KEY, PersistentDataType.STRING, lampColor.name.lowercase())
        pdc.set(FACING_KEY, PersistentDataType.STRING, facing.name)
        pdc.set(IS_FAULTY_KEY, PersistentDataType.BOOLEAN, isFaulty)
        pdc.set(FAULTY_TICK_KEY, PersistentDataType.LONG, faultyEndTick)
        pdc.set(RAINBOW_INDEX_KEY, PersistentDataType.INTEGER, rainbowColorIndex)
        pdc.set(LAST_RAINBOW_CHANGE_KEY, PersistentDataType.LONG, lastRainbowChangeTick)
    }

    init {
        // 在方块类型改变前获取并保存颜色
        val keyStr = key.key
        val colorName = keyStr.removePrefix("lamp_")
        lampColor = LampColor.fromName(colorName)
        isRainbow = colorName == "rainbow"
        setTickInterval(FaultyLamp.lampFaultTickInterval)
    }

    override fun postInitialise() {
        // 只在实体不存在时创建，避免加载时重复创建
        if (!isHeldEntityPresent("light")) {
            createDisplayEntity()
        }
        if (isRainbow) {
            updateRainbowBlock()
        }
    }

    /**
     * 创建展示实体（只在 create constructor 中调用）
     */
    private fun createDisplayEntity() {
        // 基础方块已是彩色玻璃，创建光源展示实体（略小于本体防止z-fighting）
        addEntity("light", ItemDisplayBuilder()
            .itemStack(ItemStack(if (isLit) Material.SEA_LANTERN else Material.NETHERITE_BLOCK))
            .transformation(TransformBuilder().scale(0.999f).buildForItemDisplay())
            .build(block.location.toCenterLocation()))
    }

    override fun postLoad() {
        updateDisplayEntities()
        if (isRainbow) {
            updateRainbowBlock()
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

    private fun updateRainbowBlock() {
        val color = NON_RAINBOW_COLORS[rainbowColorIndex]
        block.type = color.stainedGlassMaterial
    }

    private fun tickRainbow() {
        if (!isRainbow) return
        val currentTick = block.world.gameTime
        val interval = FaultyLamp.RAINBOW_COLOR_CHANGE_INTERVAL
        if (currentTick - lastRainbowChangeTick >= interval) {
            rainbowColorIndex = (rainbowColorIndex + 1) % NON_RAINBOW_COLORS.size
            lastRainbowChangeTick = currentTick
            updateRainbowBlock()
        }
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
        
        // 根据开关状态播放对应音效
        val sound = if (previousLit) Sound.BLOCK_STONE_BUTTON_CLICK_OFF else Sound.BLOCK_STONE_BUTTON_CLICK_ON
        player.playSound(block.location, sound, 1.0f, 1.0f)
        
        player.spawnParticle(
            org.bukkit.Particle.END_ROD,
            block.location.clone().add(0.5, 0.5, 0.5),
            5, 0.3, 0.3, 0.3, 0.1
        )
    }

    override fun onPreBlockBreak(context: io.github.pylonmc.rebar.block.context.BlockBreakContext): Boolean {
        tryRemoveAllEntities()
        return true
    }

    override fun getDropItem(context: io.github.pylonmc.rebar.block.context.BlockBreakContext): ItemStack? {
        val itemStack = defaultItem?.getItemStack()
        return itemStack ?: ItemStack(if (isRainbow) Material.WHITE_STAINED_GLASS else lampColor.stainedGlassMaterial)
    }
    
    override fun tick() {
        tickFaultyMode()
        tickRainbow()
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
