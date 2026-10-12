package io.github.rebarillumination

import io.github.pylonmc.rebar.item.RebarItem
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.rebarillumination.item.IllumarItem
import io.github.rebarillumination.util.LampColor
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material

object RebarIlluminationItems {

    fun initialize() {
        // 注册各颜色的灯物品（方块类）。
        LampColor.entries.forEach { color ->
            LampShape.entries.forEach { shape ->
                val key = shape.blockKey(color)
                val template = if (shape.usesDirectStainedGlass) {
                    // 普通灯：物品直接使用染色玻璃材质。
                    ItemStackBuilder.rebar(color.stainedGlassMaterial, key).build()
                } else {
                    // 其余形状：方块为 STRUCTURE_VOID（不可见），物品借由 ITEM_MODEL 展示染色玻璃外观。
                    ItemStackBuilder.rebar(Material.STRUCTURE_VOID, key)
                        .set(DataComponentTypes.ITEM_MODEL, color.stainedGlassMaterial.key)
                        .build()
                }
                RebarItem.register(shape.itemClass.java, template, key)
            }
        }

        // 注册荧石粉物品（无色灯没有荧石粉，排除之）。
        LampColor.entries.filter { it.hasIllumar }.forEach { color ->
            val illumarKey = LampKeys.illumarKey(color)
            val illumarTemplate = ItemStackBuilder.rebar(color.dyeMaterial, illumarKey).build()
            RebarItem.register(IllumarItem::class.java, illumarTemplate, illumarKey)
        }
    }
}
