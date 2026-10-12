package io.github.rebarillumination.block

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.block.context.BlockBreakContext
import io.github.pylonmc.rebar.block.context.BlockCreateContext
import io.github.pylonmc.rebar.block.interfaces.BlockBreakRebarBlockHandler
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock
import io.github.pylonmc.rebar.block.interfaces.EntityHolderRebarBlock
import io.github.pylonmc.rebar.block.interfaces.InteractRebarBlockHandler
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler
import io.github.pylonmc.rebar.waila.WailaDisplay
import io.github.rebarillumination.LampShape
import io.github.rebarillumination.RebarIlluminationAddon
import io.github.rebarillumination.util.LampColor
import kotlinx.coroutines.Job
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
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
import org.bukkit.util.Vector

/**
 * 所有灯具方块的公共基类。
 *
 * 重构前，LampBlock / BarLampBlock / PatchLampBlock / PillarLampBlock / SphereLampBlock
 * 五个类各自复制粘贴了大量相同逻辑（持久化读写、交互处理、开关灯音效、WAILA 文本、
 * tick 故障检测、掉落物、展示实体位置、生命周期回调等），每个类约 250–300 行，且多处
 * 出现“看起来一样实则略有偏差”的隐患（例如 SphereLampBlock 漏写 `setTickInterval`，
 * 各子类 PDC key、偏移量、音效参数各不相同且无统一约束）。
 *
 * 本基类把以上公共行为收口，子类只需提供形状相关的几何差异：
 *  - [shape]：所属 LampShape（决定注册 key 前缀，于构造时传入以避免父类初始化期访问子类属性）
 *  - [spawnDisplayEntities]：根据形状构建 shell / light 展示实体
 *  - [updateRainbowDisplay]：彩虹模式下的外观更新
 *  - 少量 open 参数（偏移距离、音效、掉落退路材质等）
 */
abstract class AbstractLampBlock(
    block: Block,
    context: BlockCreateContext,
    protected val shape: LampShape
) : RebarBlock(block, context),
    InteractRebarBlockHandler,
    BlockBreakRebarBlockHandler,
    EntityHolderRebarBlock,
    DirectionalRebarBlock,
    TickingRebarBlock,
    FaultyLamp,
    RainbowLamp {

    /** 展示实体相对方块中心的偏移距离（0 表示对齐方块中心）。 */
    protected open val displayOffset: Double = 0.0

    /** 是否在方块本身上显示材质（true 表示使用真实染色玻璃，无需贴片实体）。 */
    protected open val usesBlockTexture: Boolean = false

    /** 用于判断“实体是否已存在”的主实体名（普通灯只有 light，其余形状有 shell）。 */
    protected open val primaryEntityName: String = "shell"

    /** 开关灯音效的音量与音高（各形状略有差别）。 */
    protected open val toggleSoundVolume: Float = 1.0f
    protected open val toggleSoundPitch: Float = 1.0f

    /** 当未注册对应物品时的掉落退路材质。 */
    protected open val fallbackDropMaterial: Material
        get() = Material.STRUCTURE_VOID

    companion object {
        private val IS_LIT_KEY = NamespacedKey(RebarIlluminationAddon.instance, "is_lit")
        private val FACING_KEY = NamespacedKey(RebarIlluminationAddon.instance, "facing")
    }

    // ---- 由 FaultyLamp / RainbowLamp 接口要求、在此统一持有的状态 ----
    override var isLit: Boolean = true
    override var lampColor: LampColor = LampColor.WHITE
    override var facing: BlockFace = context.facingVertical
    override var isFaulty: Boolean = false
    override var faultyEndTick: Long = 0L
    override var isRainbow: Boolean = false
    override var lastRainbowColorIndex: Int = -1
    override var rainbowJob: Job? = null
    override var disableBlockTextureEntity: Boolean = false

    init {
        // key 形如 "{blockKeyPrefix}_{color}"，需连分隔下划线一起去掉，否则得到 "_red" 之类
        // 带前导下划线的字符串，fromName 匹配不到而回退成 WHITE，导致所有灯外壳变白。
        val colorName = key.key.removePrefix("${shape.blockKeyPrefix}_")
        lampColor = LampColor.fromName(colorName)
        isRainbow = colorName == "rainbow"
        // 统一设置故障检测 tick 间隔，修复重构前 SphereLampBlock 漏写导致的不一致。
        setTickInterval(FaultyLamp.lampFaultTickInterval)
    }

    /** 从持久化数据恢复公共状态；子类通过 [readPersistentData] 补充自身字段。 */
    protected fun readState(pdc: PersistentDataContainer) {
        isLit = pdc.get(IS_LIT_KEY, PersistentDataType.BOOLEAN) ?: true
        facing = pdc.get(FACING_KEY, PersistentDataType.STRING)?.let { BlockFace.valueOf(it) } ?: BlockFace.UP
        readPersistentData(pdc)
        isFaulty = pdc.get(IS_FAULTY_KEY, PersistentDataType.BOOLEAN) ?: false
        faultyEndTick = pdc.get(FAULTY_TICK_KEY, PersistentDataType.LONG) ?: 0L
    }

    /** 子类在此读取额外的持久化字段（如 BarLamp 的水平朝向）。 */
    protected open fun readPersistentData(pdc: PersistentDataContainer) {}

    override fun write(pdc: PersistentDataContainer) {
        pdc.set(IS_LIT_KEY, PersistentDataType.BOOLEAN, isLit)
        pdc.set(FACING_KEY, PersistentDataType.STRING, facing.name)
        writePersistentData(pdc)
        pdc.set(IS_FAULTY_KEY, PersistentDataType.BOOLEAN, isFaulty)
        pdc.set(FAULTY_TICK_KEY, PersistentDataType.LONG, faultyEndTick)
    }

    /** 子类在此写入额外的持久化字段（如 BarLamp 的水平朝向）。 */
    protected open fun writePersistentData(pdc: PersistentDataContainer) {}

    // ---- 生命周期 ----
    override fun postInitialise() {
        if (!isHeldEntityPresent(primaryEntityName)) {
            spawnDisplayEntities()
        }
        if (!usesBlockTexture) disableBlockTextureEntity = true
        if (isRainbow) startRainbowTask()
    }

    override fun postLoad() {
        if (!usesBlockTexture) disableBlockTextureEntity = true
        updateDisplayEntities()
        if (isRainbow) updateRainbowDisplay(LampColor.WHITE)
    }

    /** 由子类实现：根据形状构建 shell / light 展示实体（在 create 构造器路径中调用）。 */
    protected abstract fun spawnDisplayEntities()

    // ---- 展示实体 ----
    override fun updateDisplayEntities() {
        val lightDisplay = getHeldEntity(ItemDisplay::class.java, "light")
        val displayMaterial = when {
            !isLit -> Material.NETHERITE_BLOCK
            isCurrentlyFaulty() -> Material.REDSTONE_BLOCK
            else -> Material.SEA_LANTERN
        }
        lightDisplay?.setItemStack(ItemStack(displayMaterial))
    }

    /**
     * 展示实体（光源）所在的精确位置：方块中心沿当前 [facing] 偏移 [displayOffset]。
     * 所有形状都遵循同一套“朝向→偏移”约定，仅偏移距离不同，因此在此统一实现。
     */
    override fun getDisplayEntityLocation(): Location {
        val offset = when (facing) {
            BlockFace.UP -> Vector(0.0, -displayOffset, 0.0)
            BlockFace.DOWN -> Vector(0.0, displayOffset, 0.0)
            BlockFace.NORTH -> Vector(0.0, 0.0, displayOffset)
            BlockFace.SOUTH -> Vector(0.0, 0.0, -displayOffset)
            BlockFace.EAST -> Vector(-displayOffset, 0.0, 0.0)
            BlockFace.WEST -> Vector(displayOffset, 0.0, 0.0)
            else -> Vector(0.0, 0.0, 0.0)
        }
        return block.location.toCenterLocation().add(offset)
    }

    // ---- 交互 ----
    @MultiHandler(priorities = [EventPriority.NORMAL, EventPriority.MONITOR])
    override fun onInteractedWith(event: PlayerInteractEvent, priority: EventPriority) {
        if (!event.action.isRightClick
            || event.hand != EquipmentSlot.HAND
            || event.useInteractedBlock() == Event.Result.DENY) {
            return
        }

        // 故障模式下：潜行禁止切换；空手右键尝试修复。
        if (isFaulty) {
            if (event.player.isSneaking) {
                if (priority == EventPriority.NORMAL) {
                    event.setUseItemInHand(Event.Result.DENY)
                }
                return
            }
            if (tryRightClickFix(event, priority)) {
                return
            }
        }

        // 潜行右键：开关灯。
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
        val previousLit = isLit
        isLit = !isLit
        updateDisplayEntities()

        val sound = if (previousLit) Sound.BLOCK_STONE_BUTTON_CLICK_OFF else Sound.BLOCK_STONE_BUTTON_CLICK_ON
        player.playSound(getDisplayEntityLocation(), sound, toggleSoundVolume, toggleSoundPitch)
    }

    // ---- 破坏 / 掉落 / tick ----
    override fun onPreBlockBreak(context: BlockBreakContext): Boolean {
        stopRainbowTask()
        tryRemoveAllEntities()
        return true
    }

    override fun getDropItem(context: BlockBreakContext): ItemStack? {
        return defaultItem?.createNewItemStack() ?: ItemStack(fallbackDropMaterial)
    }

    override fun tick() {
        tickFaultyMode()
    }

    // ---- WAILA ----
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
