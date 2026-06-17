package io.github.rebarillumination

import io.github.pylonmc.rebar.item.RebarItem
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.rebarillumination.item.BarLampItem
import io.github.rebarillumination.item.IllumarItem
import io.github.rebarillumination.item.LampItem
import io.github.rebarillumination.item.PillarLampItem
import io.github.rebarillumination.item.SphereLampItem
import io.github.rebarillumination.item.PatchLampItem
import io.github.rebarillumination.util.LampColor
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey

object RebarIlluminationItems {

    fun initialize() {
        // 注册所有灯类型
        LampColor.entries.forEach { color ->
            val colorName = color.name.lowercase()

            // 注册方块灯物品
            val lampKey = NamespacedKey(RebarIlluminationAddon.instance, "lamp_$colorName")
            val lampTemplate = ItemStackBuilder.rebar(color.stainedGlassMaterial, lampKey).build()
            RebarItem.register(LampItem::class.java, lampTemplate, lampKey)

            // 注册柱状灯物品
            val pillarLampKey = NamespacedKey(RebarIlluminationAddon.instance, "pillar_lamp_$colorName")
            val pillarLampTemplate = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, pillarLampKey)
                .set(DataComponentTypes.ITEM_MODEL, color.stainedGlassMaterial.key)
                .build()
            RebarItem.register(PillarLampItem::class.java, pillarLampTemplate, pillarLampKey)

            // 注册条状灯物品
            val barLampKey = NamespacedKey(RebarIlluminationAddon.instance, "bar_lamp_$colorName")
            val barLampTemplate = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, barLampKey)
                .set(DataComponentTypes.ITEM_MODEL, color.stainedGlassMaterial.key)
                .build()
            RebarItem.register(BarLampItem::class.java, barLampTemplate, barLampKey)

            // 注册球形灯物品
            val sphereLampKey = NamespacedKey(RebarIlluminationAddon.instance, "sphere_lamp_$colorName")
            val sphereLampTemplate = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, sphereLampKey)
                .set(DataComponentTypes.ITEM_MODEL, color.stainedGlassMaterial.key)
                .build()
            RebarItem.register(SphereLampItem::class.java, sphereLampTemplate, sphereLampKey)

            // 注册贴片灯物品
            val patchLampKey = NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_$colorName")
            val patchLampTemplate = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, patchLampKey)
                .set(DataComponentTypes.ITEM_MODEL, color.stainedGlassMaterial.key)
                .build()
            RebarItem.register(PatchLampItem::class.java, patchLampTemplate, patchLampKey)
        }

        // 注册荧石粉物品（不包括彩虹和无色）
        LampColor.entries.filter { it.hasIllumar && it != LampColor.RAINBOW }.forEach { color ->
            val colorName = color.name.lowercase()
            val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
            val illumarTemplate = ItemStackBuilder.rebar(color.dyeMaterial, illumarKey).build()
            RebarItem.register(IllumarItem::class.java, illumarTemplate, illumarKey)
        }
    }
}
