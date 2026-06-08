package io.github.rebarillumination

import io.github.pylonmc.rebar.item.RebarItem
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.item.research.Research
import io.github.pylonmc.rebar.registry.RebarRegistry
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey

object RebarIlluminationResearch {

    fun initialize() {
        val colors = listOf(
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
        )

        val unlocks = mutableSetOf<NamespacedKey>()
        
        colors.forEach { color ->
            unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "illumar_$color"))
            unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "lamp_$color"))
            unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "bar_lamp_$color"))
            unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "pillar_lamp_$color"))
            unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "sphere_lamp_$color"))
            unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "wall_lamp_$color"))
        }

        val rainbow = "rainbow"
        unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "illumar_$rainbow"))
        unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "lamp_$rainbow"))
        unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "bar_lamp_$rainbow"))
        unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "pillar_lamp_$rainbow"))
        unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "sphere_lamp_$rainbow"))
        unlocks.add(NamespacedKey(RebarIlluminationAddon.instance, "wall_lamp_$rainbow"))

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