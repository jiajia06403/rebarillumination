package io.github.rebarillumination

import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.item.research.Research
import io.github.rebarillumination.util.LampColor
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey

object RebarIlluminationResearch {

    fun initialize() {
        val unlocks = mutableSetOf<NamespacedKey>()

        // 16 种标准颜色（不含彩虹与无色）各自的荧石粉与五种灯。
        LampColor.entries
            .filter { it != LampColor.RAINBOW && it != LampColor.COLORLESS }
            .forEach { color ->
                unlocks.add(LampKeys.illumarKey(color))
                LampShape.entries.forEach { shape -> unlocks.add(shape.blockKey(color)) }
            }

        // 彩虹系列。
        unlocks.add(LampKeys.illumarKey(LampColor.RAINBOW))
        LampShape.entries.forEach { shape -> unlocks.add(shape.blockKey(LampColor.RAINBOW)) }

        val researchKey = NamespacedKey(RebarIlluminationAddon.instance, "illumination")
        val icon = ItemStackBuilder.rebar(Material.GLOWSTONE_DUST, researchKey).build()

        val research = Research(
            key = researchKey,
            itemTemplate = icon,
            name = Component.translatable("rebarillumination.research.illumination_basic"),
            cost = 50,
            unlocks = unlocks
        )

        research.register()
    }
}
