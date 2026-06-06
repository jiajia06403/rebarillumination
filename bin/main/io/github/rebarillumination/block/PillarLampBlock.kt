package io.github.rebarillumination.block

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.block.base.RebarBreakHandler
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock
import io.github.pylonmc.rebar.block.base.RebarEntityHolderBlock
import io.github.pylonmc.rebar.block.base.RebarInteractBlock
import io.github.pylonmc.rebar.block.base.RebarTickingBlock
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

class PillarLampBlock(
    block: Block,
    context: BlockCreateContext
) : RebarBlock(block, context), RebarInteractBlock, RebarBreakHandler, RebarEntityHolderBlock, RebarDirectionalBlock, RebarTickingBlock, FaultyLamp {

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
        pdc.set(IS_LIT_KEY, PersistentDataType.BOOLEAN, _isLit)
        pdc.set(FACING_KEY, PersistentDataType.STRING, facing.name)
        pdc.set(IS_FAULTY_KEY, PersistentDataType.BOOLEAN, isFaulty)
        pdc.set(FAULTY_TICK_KEY, PersistentDataType.LONG, faultyEndTick)
    }

    override var lampColor: LampColor = LampColor.WHITE
        get() {
            val keyStr = key.key
            val colorName = keyStr.removePrefix("pillar_lamp_")
            return LampColor.fromName(colorName)
        }

    init {
        setTickInterval(FaultyLamp.lampFaultTickInterval)
    }

    override fun postInitialise() {
        // 只在实体不存在时创建，避免加载时重复创建
        if (!isHeldEntityPresent("shell")) {
            createDisplayEntities()
        }
    }

    /**
     * 创建展示实体（只在 create constructor 中调用）
     */
    private fun createDisplayEntities() {
        val blockFacing = this.facing

        // 根据朝向计算偏移（约0.2单位）
        var offsetX = 0.0
        var offsetY = 0.0
        var offsetZ = 0.0

        when (blockFacing) {
            BlockFace.NORTH -> offsetZ = 0.2
            BlockFace.SOUTH -> offsetZ = -0.2
            BlockFace.EAST -> offsetX = -0.2
            BlockFace.WEST -> offsetX = 0.2
            BlockFace.UP -> offsetY = -0.2
            BlockFace.DOWN -> offsetY = 0.2
            else -> offsetZ = 0.2
        }

        val centerLoc = block.location.toCenterLocation().add(offsetX, offsetY, offsetZ)

        // 柱状灯是柱体，长轴在Y方向
        // 根据朝向正确旋转柱体，让长轴指向目标方向
        val transformBuilder = TransformBuilder()
        val lightTransform = TransformBuilder()
        
        when (blockFacing) {
            BlockFace.NORTH -> {
                // 水平放置，长轴朝向北方 (-Z)
                transformBuilder.rotate(Math.PI / 2, 0.0, 0.0)
                lightTransform.rotate(Math.PI / 2, 0.0, 0.0)
            }
            BlockFace.SOUTH -> {
                // 水平放置，长轴朝向南方 (+Z)
                transformBuilder.rotate(-Math.PI / 2, 0.0, 0.0)
                lightTransform.rotate(-Math.PI / 2, 0.0, 0.0)
            }
            BlockFace.EAST -> {
                // 水平放置，长轴朝向东 (+X)
                transformBuilder.rotate(0.0, 0.0, -Math.PI / 2)
                lightTransform.rotate(0.0, 0.0, -Math.PI / 2)
            }
            BlockFace.WEST -> {
                // 水平放置，长轴朝向西 (-X)
                transformBuilder.rotate(0.0, 0.0, Math.PI / 2)
                lightTransform.rotate(0.0, 0.0, Math.PI / 2)
            }
            BlockFace.DOWN -> {
                // 竖直放置，向下（倒置）
                transformBuilder.rotate(Math.PI, 0.0, 0.0)
                lightTransform.rotate(Math.PI, 0.0, 0.0)
            }
            else -> {
                // 竖直放置，向上（不需要旋转）
            }
        }
        
        transformBuilder.scale(0.301f, 0.801f, 0.301f)
        lightTransform.scale(0.2f, 0.6f, 0.2f)

        addEntity("shell", ItemDisplayBuilder()
            .itemStack(ItemStack(lampColor.stainedGlassMaterial))
            .transformation(transformBuilder.buildForItemDisplay())
            .build(centerLoc))

        addEntity("light", ItemDisplayBuilder()
            .itemStack(ItemStack(if (isLit) Material.SEA_LANTERN else Material.NETHERITE_BLOCK))
            .transformation(lightTransform.buildForItemDisplay())
            .build(centerLoc))
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