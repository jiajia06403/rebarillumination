package io.github.rebarillumination.block

import io.github.pylonmc.rebar.config.ConfigSection
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter
import io.github.pylonmc.rebar.item.RebarItem
import io.github.rebarillumination.RebarIlluminationAddon
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

interface FaultyLamp {
    val block: Block
    val isLit: Boolean
    var lampColor: LampColor
    fun updateDisplayEntities()
    fun write(pdc: PersistentDataContainer)
    
    val IS_FAULTY_KEY get() = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "is_faulty")
    val FAULTY_TICK_KEY get() = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "faulty_tick")
    
    var isFaulty: Boolean
    var faultyEndTick: Long
    
    /**
     * 获取展示实体的位置（默认使用方块中心，子类可重写）
     */
    fun getDisplayEntityLocation(): org.bukkit.Location {
        return block.location.toCenterLocation()
    }

    companion object {
        private val CONFIG_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "decoration_lamp")
        
        val lampFaultTickInterval: Int by lazy {
            ConfigSection.fromSettings(CONFIG_KEY)
                .get("lamp-fault-tick-interval", ConfigAdapter.INTEGER, 2)
        }
        
        private val FAULT_CHANCE: Double by lazy {
            ConfigSection.fromSettings(CONFIG_KEY)
                .get("fault-chance", ConfigAdapter.DOUBLE, 0.02)
        }
        
        private val MIN_FAULT_DURATION_INTERVALS: Int by lazy {
            ConfigSection.fromSettings(CONFIG_KEY)
                .get("min-fault-duration-intervals", ConfigAdapter.INTEGER, 1)
        }
        
        private val MAX_FAULT_DURATION_INTERVALS: Int by lazy {
            ConfigSection.fromSettings(CONFIG_KEY)
                .get("max-fault-duration-intervals", ConfigAdapter.INTEGER, 20)
        }

        val RIGHT_CLICK_FIX_CHANCE: Double by lazy {
            ConfigSection.fromSettings(CONFIG_KEY)
                .get("right-click-fix-chance", ConfigAdapter.DOUBLE, 0.05)
        }

        val RAINBOW_COLOR_CHANGE_INTERVAL: Int by lazy {
            ConfigSection.fromSettings(CONFIG_KEY)
                .get("rainbow-color-change-interval", ConfigAdapter.INTEGER, 10)
        }
    }
    
    fun handleFaultyModeInteraction(event: PlayerInteractEvent, priority: EventPriority) {
        if (!event.action.isRightClick || event.hand != EquipmentSlot.HAND) return
        
        val item = event.item ?: return
        val player = event.player
        
        if (item.type == Material.FLINT && !isFaulty) {
            if (priority == EventPriority.NORMAL) {
                event.setUseItemInHand(org.bukkit.event.Event.Result.DENY)
                return
            }
            enableFaultyMode(player)
            return
        }
        
        if (!isFaulty) return
        
        val rebarItem = RebarItem.fromStack(item)
        
        if (rebarItem != null) {
            val itemKey = rebarItem.key
            val keyName = itemKey.key
            
            for (color in LampColor.entries) {
                if (keyName == "illumar_${color.name.lowercase()}") {
                    if (color != lampColor) {
                        return
                    }
                    if (priority == EventPriority.NORMAL) {
                        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY)
                        return
                    }
                    disableFaultyMode(player)
                    break
                }
            }
        }
    }

    fun tryRightClickFix(event: PlayerInteractEvent, priority: EventPriority): Boolean {
        if (!isFaulty || !event.action.isRightClick || event.hand != EquipmentSlot.HAND) return false
        
        val item = event.item
        if (item != null && item.type != Material.AIR) return false

        if (priority == EventPriority.NORMAL) {
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY)
            return true
        }

        if (Random.nextDouble() <= RIGHT_CLICK_FIX_CHANCE) {
            disableFaultyMode(event.player)
        } else {
            val displayLoc = getDisplayEntityLocation()
            event.player.playSound(displayLoc, Sound.BLOCK_ANVIL_LAND, 0.1f, 0.8f)
            event.player.spawnParticle(
                org.bukkit.Particle.SMOKE,
                displayLoc,
                5, 0.15, 0.15, 0.15, 0.05
            )
        }
        return true
    }
    
    private fun enableFaultyMode(player: Player) {
        isFaulty = true
        val displayLoc = getDisplayEntityLocation()
        player.playSound(displayLoc, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 1.0f, 0.8f)
        
        val dustOptions = org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f)
        player.spawnParticle(
            org.bukkit.Particle.DUST,
            displayLoc,
            5, 0.15, 0.15, 0.15, 0.05,
            dustOptions
        )
        updateDisplayEntities()
    }

    private fun disableFaultyMode(player: Player) {
        isFaulty = false
        faultyEndTick = 0
        
        val displayLoc = getDisplayEntityLocation()
        player.playSound(displayLoc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f)
        player.spawnParticle(
            org.bukkit.Particle.HAPPY_VILLAGER,
            displayLoc,
            5, 0.15, 0.15, 0.15, 0.05
        )
        updateDisplayEntities()
    }
    
    fun tickFaultyMode() {
        if (!isLit || !isFaulty) return
        val currentTick = block.world.gameTime
        val wasFaulting = faultyEndTick > 0

        if (wasFaulting) {
            if (currentTick >= faultyEndTick) {
                faultyEndTick = 0
                updateDisplayEntities()
            }
            return
        }
        
        if (Random.nextDouble() <= FAULT_CHANCE) {
            val durationIntervals = Random.nextInt(MIN_FAULT_DURATION_INTERVALS, MAX_FAULT_DURATION_INTERVALS + 1)
            faultyEndTick = currentTick + durationIntervals * lampFaultTickInterval.toLong()
            updateDisplayEntities()
        }
    }
    
    fun isCurrentlyFaulty(): Boolean {
        return isFaulty && faultyEndTick > 0
    }
}