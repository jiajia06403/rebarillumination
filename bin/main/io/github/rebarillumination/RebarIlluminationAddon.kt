package io.github.rebarillumination

import io.github.pylonmc.rebar.addon.RebarAddon
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class RebarIlluminationAddon : JavaPlugin(), RebarAddon {

    companion object {
        lateinit var instance: RebarIlluminationAddon
            private set
    }

    override val javaPlugin: JavaPlugin
        get() = this

    override val languages: Set<Locale>
        get() = setOf(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE)

    override val material: Material
        get() = Material.REDSTONE_LAMP

    override fun onEnable() {
        instance = this
        registerWithRebar()

        RebarIlluminationItems.initialize()
        RebarIlluminationBlocks.initialize()
        RebarIlluminationRecipes.initialize()
        RebarIlluminationResearch.initialize()
        RebarIlluminationGuide.initialize()
    }
}
