package io.github.rebarillumination.block

import io.github.pylonmc.rebar.config.ConfigSection
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter
import io.github.pylonmc.rebar.util.delayTicks
import io.github.rebarillumination.RebarIlluminationAddon
import io.github.rebarillumination.util.LampColor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block

interface RainbowLamp {
    val block: Block
    
    var isRainbow: Boolean
    var lastRainbowColorIndex: Int
    
    var rainbowJob: kotlinx.coroutines.Job?
    
    companion object {
        private val CONFIG_KEY = org.bukkit.NamespacedKey(RebarIlluminationAddon.instance, "decoration_lamp")
        
        val RAINBOW_COLOR_CHANGE_INTERVAL: Int by lazy {
            ConfigSection.fromSettings(CONFIG_KEY)
                .get("rainbow-color-change-interval", ConfigAdapter.INTEGER, 10)
        }
        
        // Minecraft 16 种染色玻璃颜色
        val RAINBOW_COLORS by lazy {
            listOf(
                LampColor.WHITE,
                LampColor.LIGHT_GRAY,
                LampColor.GRAY,
                LampColor.BLACK,
                LampColor.BROWN,
                LampColor.RED,
                LampColor.ORANGE,
                LampColor.YELLOW,
                LampColor.LIME,
                LampColor.GREEN,
                LampColor.CYAN,
                LampColor.LIGHT_BLUE,
                LampColor.BLUE,
                LampColor.PURPLE,
                LampColor.MAGENTA,
                LampColor.PINK
            )
        }
    }
    
    fun updateRainbowDisplay(color: LampColor)
    
    fun startRainbowTask() {
        if (!isRainbow) return
        
        rainbowJob = GlobalScope.launch {
            while (isRainbow && block.world.isChunkLoaded(block.chunk)) {
                val currentTick = block.world.gameTime
                val interval = RAINBOW_COLOR_CHANGE_INTERVAL
                val colorIndex = ((currentTick / interval) % RAINBOW_COLORS.size).toInt()
                
                if (colorIndex != lastRainbowColorIndex) {
                    lastRainbowColorIndex = colorIndex
                    val color = RAINBOW_COLORS[colorIndex]
                    // 使用 Bukkit 调度器在主线程执行
                    Bukkit.getScheduler().runTask(RebarIlluminationAddon.instance, Runnable {
                        updateRainbowDisplay(color)
                    })
                }
                
                delayTicks(1)
            }
        }
    }
    
    fun stopRainbowTask() {
        rainbowJob?.cancel()
        rainbowJob = null
    }
    
    fun getRainbowColorForTick(tick: Long): LampColor {
        val interval = RAINBOW_COLOR_CHANGE_INTERVAL
        val colorIndex = ((tick / interval) % RAINBOW_COLORS.size).toInt()
        return RAINBOW_COLORS[colorIndex]
    }
}