package io.github.rebarillumination

import io.github.pylonmc.rebar.item.RebarItem
import io.github.rebarillumination.block.AbstractLampBlock
import io.github.rebarillumination.block.BarLampBlock
import io.github.rebarillumination.block.LampBlock
import io.github.rebarillumination.block.PatchLampBlock
import io.github.rebarillumination.block.PillarLampBlock
import io.github.rebarillumination.block.SphereLampBlock
import io.github.rebarillumination.item.BarLampItem
import io.github.rebarillumination.item.LampItem
import io.github.rebarillumination.item.PatchLampItem
import io.github.rebarillumination.item.PillarLampItem
import io.github.rebarillumination.item.SphereLampItem
import io.github.rebarillumination.util.LampColor
import kotlin.reflect.KClass
import org.bukkit.Material
import org.bukkit.NamespacedKey

/**
 * 灯具形状的集中登记处。
 *
 * 重构前，形如 `"lamp_"` / `"pillar_lamp_"` / `"bar_lamp_"` / `"sphere_lamp_"` /
 * `"patch_lamp_"` / `"illumar_"` 的 key 前缀被硬编码在 Blocks / Items / Recipes /
 * Research / Guide / FaultyLampMixin 等至少 6 个文件中。一旦新增形状或改名，极易漏改，
 * 是典型的“隐性耦合”隐患。这里把形状与对应的方块/物品类、key 前缀统一登记，
 * 新增形状只需在此处添加一项。
 */
enum class LampShape(
    /** 方块 / 物品注册 key 的前缀，例如 `"lamp"` 对应 `"lamp_white"`。 */
    val blockKeyPrefix: String,
    val blockClass: KClass<out AbstractLampBlock>,
    val itemClass: KClass<out RebarItem>,
    /**
     * true 表示方块本身即染色玻璃（LampBlock），无需贴片展示实体，物品也直接使用该材质；
     * false 表示方块为 STRUCTURE_VOID（不可见），需依靠 shell 展示实体并借助 ITEM_MODEL 显示外观。
     */
    val usesDirectStainedGlass: Boolean
) {
    LAMP("lamp", LampBlock::class, LampItem::class, true),
    PILLAR("pillar_lamp", PillarLampBlock::class, PillarLampItem::class, false),
    BAR("bar_lamp", BarLampBlock::class, BarLampItem::class, false),
    SPHERE("sphere_lamp", SphereLampBlock::class, SphereLampItem::class, false),
    PATCH("patch_lamp", PatchLampBlock::class, PatchLampItem::class, false);

    /** 该形状在某颜色下对应的方块 / 物品注册 key。 */
    fun blockKey(color: LampColor): NamespacedKey =
        NamespacedKey(RebarIlluminationAddon.instance, "${blockKeyPrefix}_${color.name.lowercase()}")
}

/**
 * 与命名相关的工具函数。目前仅封装荧石粉（illumar）物品的 key 构造，
 * 使原本散落在多处的 `"illumar_xxx"` 魔法字符串收敛到单一来源。
 */
object LampKeys {
    const val ILLUMAR_PREFIX = "illumar"

    /** 某颜色荧石粉物品对应的注册 key。 */
    fun illumarKey(color: LampColor): NamespacedKey =
        NamespacedKey(RebarIlluminationAddon.instance, "${ILLUMAR_PREFIX}_${color.name.lowercase()}")

    /** 仅用于配方等需要拼接后缀（如 `_top` / `_center`）的场景。 */
    fun shapeKeyPrefix(shape: LampShape): String = shape.blockKeyPrefix
}
