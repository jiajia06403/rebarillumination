package io.github.rebarillumination

import io.github.pylonmc.rebar.addon.RebarAddon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class RebarIlluminationAddon : JavaPlugin(), RebarAddon {

    companion object {
        lateinit var instance: RebarIlluminationAddon
            private set
        // 绑定插件生命周期的协程作用域。取代 GlobalScope，避免协程泄漏；
        // onDisable 时统一取消，防止卸载 / reload 后协程继续访问已失效的插件对象。
        lateinit var scope: CoroutineScope
            private set
    }

    override val javaPlugin: JavaPlugin
        get() = this

    override val defaultLanguage: Locale
        get() = Locale.ENGLISH

    override val material: Material
        get() = Material.REDSTONE_LAMP

    override fun onEnable() {
        instance = this
        // 每次启用（含 /reload）重建作用域，确保之前的取消不会影响新一次启动。
        scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        registerWithRebar()

        RebarIlluminationItems.initialize()
        RebarIlluminationBlocks.initialize()
        RebarIlluminationRecipes.initialize()
        RebarIlluminationResearch.initialize()
        RebarIlluminationGuide.initialize()
    }

    override fun onDisable() {
        scope.cancel()
    }
}
