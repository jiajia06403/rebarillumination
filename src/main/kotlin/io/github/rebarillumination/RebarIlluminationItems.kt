package io.github.rebarillumination

import io.github.pylonmc.rebar.item.RebarItem
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.rebarillumination.item.BarLampItem
import io.github.rebarillumination.item.IllumarItem
import io.github.rebarillumination.item.LampItem
import io.github.rebarillumination.item.PillarLampItem
import io.github.rebarillumination.item.SphereLampItem
import io.github.rebarillumination.item.WallLampItem
import io.github.rebarillumination.util.LampColor
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey

object RebarIlluminationItems {

    private val colors = listOf(
        LampColor.WHITE, LampColor.ORANGE, LampColor.MAGENTA, LampColor.LIGHT_BLUE,
        LampColor.YELLOW, LampColor.LIME, LampColor.PINK, LampColor.GRAY,
        LampColor.LIGHT_GRAY, LampColor.CYAN, LampColor.PURPLE, LampColor.BLUE,
        LampColor.BROWN, LampColor.GREEN, LampColor.RED, LampColor.BLACK
    )

    fun initialize() {
        colors.forEach { color ->
            val colorName = color.name.lowercase()

            // 注册荧光粉物品 - 只有这个用染料
            val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
            val illumarTemplate = ItemStackBuilder.rebar(color.dyeMaterial, illumarKey).build()
            RebarItem.register(IllumarItem::class.java, illumarTemplate, illumarKey)

            // 注册方块灯物品（关联到方块）- 使用对应颜色的染色玻璃
            val lampKey = NamespacedKey(RebarIlluminationAddon.instance, "lamp_$colorName")
            val lampTemplate = ItemStackBuilder.rebar(color.stainedGlassMaterial, lampKey)
                .build()
            RebarItem.register(LampItem::class.java, lampTemplate, lampKey)

            // 注册柱状灯物品（关联到方块）- 使用结构空位，但ITEM_MODEL显示染色玻璃
            val pillarLampKey = NamespacedKey(RebarIlluminationAddon.instance, "pillar_lamp_$colorName")
            val pillarLampTemplate = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, pillarLampKey)
                .set(DataComponentTypes.ITEM_MODEL, color.stainedGlassMaterial.key)
                .build()
            RebarItem.register(PillarLampItem::class.java, pillarLampTemplate, pillarLampKey)

            // 注册条状灯物品（关联到方块）- 使用结构空位，但ITEM_MODEL显示染色玻璃
            val barLampKey = NamespacedKey(RebarIlluminationAddon.instance, "bar_lamp_$colorName")
            val barLampTemplate = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, barLampKey)
                .set(DataComponentTypes.ITEM_MODEL, color.stainedGlassMaterial.key)
                .build()
            RebarItem.register(BarLampItem::class.java, barLampTemplate, barLampKey)

            // 注册球形灯物品（关联到方块）- 使用结构空位，但ITEM_MODEL显示染色玻璃
            val sphereLampKey = NamespacedKey(RebarIlluminationAddon.instance, "sphere_lamp_$colorName")
            val sphereLampTemplate = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, sphereLampKey)
                .set(DataComponentTypes.ITEM_MODEL, color.stainedGlassMaterial.key)
                .build()
            RebarItem.register(SphereLampItem::class.java, sphereLampTemplate, sphereLampKey)

            // 注册壁灯物品（关联到方块）- 使用结构空位，但ITEM_MODEL显示染色玻璃
            val wallLampKey = NamespacedKey(RebarIlluminationAddon.instance, "wall_lamp_$colorName")
            val wallLampTemplate = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, wallLampKey)
                .set(DataComponentTypes.ITEM_MODEL, color.stainedGlassMaterial.key)
                .build()
            RebarItem.register(WallLampItem::class.java, wallLampTemplate, wallLampKey)
        }
    }
}