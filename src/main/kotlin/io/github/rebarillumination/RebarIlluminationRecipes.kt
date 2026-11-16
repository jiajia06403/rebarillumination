package io.github.rebarillumination

import io.github.pylonmc.rebar.item.ItemTypeWrapper
import io.github.pylonmc.rebar.item.RebarItemSchema
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItem
import io.github.pylonmc.rebar.recipe.ingredient.ItemChoice
import io.github.pylonmc.rebar.recipe.vanilla.ShapedRecipeType
import io.github.pylonmc.rebar.recipe.vanilla.ShapelessRecipeType
import io.github.pylonmc.rebar.recipe.vanilla.ShapedRebarRecipe
import io.github.pylonmc.rebar.recipe.vanilla.ShapelessRebarRecipe
import io.github.pylonmc.rebar.registry.RebarRegistry
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.recipe.CraftingBookCategory

object RebarIlluminationRecipes {

    /** 改色目标色 = 除无色外的全部颜色（16 标准色 + 彩虹），同时决定所需荧石粉的颜色。 */
    private val RECOLOR_TARGETS = LampColor.entries.filter { it != LampColor.COLORLESS }

    /** 改色原料 = 全部 18 种灯物品（含彩虹与无色），使用时按目标色排除同色。 */
    private val RECOLOR_SOURCES = LampColor.entries

    /** 可被水桶洗白的灯 = 16 标准色 + 彩虹（无色灯本身已是无色）。 */
    private val WASHABLE_SOURCES = RECOLOR_TARGETS

    fun initialize() {
        // 注册荧石粉配方（不含彩虹；无色没有荧石粉）。
        LampColor.entries.filter { it.hasIllumar && it != LampColor.RAINBOW }.forEach { color ->
            registerIllumarRecipe(color)
        }

        registerRainbowIllumarRecipe()

        // 注册所有形状的灯配方。
        LampColor.entries.forEach { color ->
            registerLampRecipe(color)
            registerPillarLampRecipe(color)
            registerBarLampRecipe(color)
            registerSphereLampRecipe(color)
            registerWallLampRecipe(color)
        }

        // 改色：灯 + 荧石粉 → 新颜色的同类灯（排除同色；彩虹灯也可直接染成单色）。
        registerRecolorRecipes()

        // 洗白：有色灯 + 水桶 → 无色灯（空桶由原版 craftRemainder 自动返还）。
        registerWashRecipes()
    }

    private fun registerIllumarRecipe(color: LampColor) {
        val illumarKey = LampKeys.illumarKey(color)
        val schema = RebarRegistry.ITEMS[illumarKey] ?: return
        val illumarItem = schema.createNewItemStack()
        illumarItem.amount = 8

        val recipe = ShapedRecipe(illumarKey, illumarItem)
            .shape("GGG", "GDG", "GGG")
            .setIngredient('G', Material.GLOWSTONE_DUST)
            .setIngredient('D', color.dyeMaterial)

        ShapedRecipeType.addRecipe(ShapedRebarRecipe.fromVanilla(recipe))
    }

    private fun registerRainbowIllumarRecipe() {
        val rainbowIllumarKey = LampKeys.illumarKey(LampColor.RAINBOW)
        val schema = RebarRegistry.ITEMS[rainbowIllumarKey] ?: return
        val rainbowIllumarItem = schema.createNewItemStack()
        rainbowIllumarItem.amount = 4

        // 红橙黄绿青蓝紫 7 种颜色。
        val rainbowColors = listOf(
            LampColor.RED,
            LampColor.ORANGE,
            LampColor.YELLOW,
            LampColor.GREEN,
            LampColor.CYAN,
            LampColor.BLUE,
            LampColor.PURPLE
        )

        // 获取这 7 种颜色的荧石粉物品。
        val illumarItems = rainbowColors.mapNotNull { color ->
            RebarRegistry.ITEMS[LampKeys.illumarKey(color)]?.createNewItemStack()
        }

        if (illumarItems.size < 7) return

        // 无序合成配方。
        val recipe = ShapelessRecipe(rainbowIllumarKey, rainbowIllumarItem)
        illumarItems.forEach { recipe.addIngredient(it) }

        ShapelessRecipeType.addRecipe(ShapelessRebarRecipe.fromVanilla(recipe))
    }

    private fun registerLampRecipe(color: LampColor) {
        val lampKey = LampShape.LAMP.blockKey(color)
        val schema = RebarRegistry.ITEMS[lampKey] ?: return
        val lampItem = schema.createNewItemStack()

        val recipe = if (color.hasIllumar) {
            val illumarItem = illumarItemOf(color) ?: return
            ShapedRecipe(lampKey, lampItem)
                .shape("NNN", "NIN", "NNN")
                .setIngredient('I', RecipeChoice.exactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
        } else {
            ShapedRecipe(lampKey, lampItem)
                .shape("NNN", "NGN", "NNN")
                .setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
        }

        ShapedRecipeType.addRecipe(ShapedRebarRecipe.fromVanilla(recipe))
    }

    private fun registerPillarLampRecipe(color: LampColor) {
        val pillarLampKey = LampShape.PILLAR.blockKey(color)
        val schema = RebarRegistry.ITEMS[pillarLampKey] ?: return
        val pillarLampItem = schema.createNewItemStack()

        val recipe = if (color.hasIllumar) {
            val illumarItem = illumarItemOf(color) ?: return
            ShapedRecipe(pillarLampKey, pillarLampItem)
                .shape(" N ", " I ", " N ")
                .setIngredient('I', RecipeChoice.exactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
        } else {
            ShapedRecipe(pillarLampKey, pillarLampItem)
                .shape(" N ", " G ", " N ")
                .setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
        }

        ShapedRecipeType.addRecipe(ShapedRebarRecipe.fromVanilla(recipe))
    }

    private fun registerBarLampRecipe(color: LampColor) {
        val barLampKey = LampShape.BAR.blockKey(color)
        val schema = RebarRegistry.ITEMS[barLampKey] ?: return
        val barLampItem = schema.createNewItemStack()

        val recipe = if (color.hasIllumar) {
            val illumarItem = illumarItemOf(color) ?: return
            ShapedRecipe(barLampKey, barLampItem)
                .shape("NIN")
                .setIngredient('I', RecipeChoice.exactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
        } else {
            ShapedRecipe(barLampKey, barLampItem)
                .shape("NGN")
                .setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
        }

        ShapedRecipeType.addRecipe(ShapedRebarRecipe.fromVanilla(recipe))
    }

    private fun registerSphereLampRecipe(color: LampColor) {
        val sphereLampKey = LampShape.SPHERE.blockKey(color)
        val schema = RebarRegistry.ITEMS[sphereLampKey] ?: return
        val sphereLampItem = schema.createNewItemStack()

        val recipe = if (color.hasIllumar) {
            val illumarItem = illumarItemOf(color) ?: return
            ShapedRecipe(sphereLampKey, sphereLampItem)
                .shape(" N ", "NIN", " N ")
                .setIngredient('I', RecipeChoice.exactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
        } else {
            ShapedRecipe(sphereLampKey, sphereLampItem)
                .shape(" N ", "NGN", " N ")
                .setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
        }

        ShapedRecipeType.addRecipe(ShapedRebarRecipe.fromVanilla(recipe))
    }

    private fun registerWallLampRecipe(color: LampColor) {
        val patchLampKey = LampShape.PATCH.blockKey(color)
        val schema = RebarRegistry.ITEMS[patchLampKey] ?: return
        val wallLampItem = schema.createNewItemStack()
        val suffix = color.name.lowercase()

        val (shapeA, shapeB) = if (color.hasIllumar) {
            val illumarItem = illumarItemOf(color) ?: return
            val top = ShapedRecipe(
                NamespacedKey(RebarIlluminationAddon.instance, "${LampShape.PATCH.blockKeyPrefix}_${suffix}_top"),
                wallLampItem.clone()
            ).shape("NNN", " I ").setIngredient('I', RecipeChoice.exactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
            val center = ShapedRecipe(
                NamespacedKey(RebarIlluminationAddon.instance, "${LampShape.PATCH.blockKeyPrefix}_${suffix}_center"),
                wallLampItem.clone()
            ).shape(" I ", "NNN").setIngredient('I', RecipeChoice.exactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
            top to center
        } else {
            val top = ShapedRecipe(
                NamespacedKey(RebarIlluminationAddon.instance, "${LampShape.PATCH.blockKeyPrefix}_${suffix}_top"),
                wallLampItem.clone()
            ).shape("NNN", " G ").setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
            val center = ShapedRecipe(
                NamespacedKey(RebarIlluminationAddon.instance, "${LampShape.PATCH.blockKeyPrefix}_${suffix}_center"),
                wallLampItem.clone()
            ).shape(" G ", "NNN").setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
            top to center
        }

        ShapedRecipeType.addRecipe(ShapedRebarRecipe.fromVanilla(shapeA))
        ShapedRecipeType.addRecipe(ShapedRebarRecipe.fromVanilla(shapeB))
    }

    /** 取得某颜色荧石粉的物品副本（无荧石粉时返回 null）。 */
    private fun illumarItemOf(color: LampColor): ItemStack? {
        return RebarRegistry.ITEMS[LampKeys.illumarKey(color)]?.createNewItemStack()
    }

    /** 按 Rebar 物品 schema 匹配的原料选择器（不比较组件，避免实例级差异导致失配）。 */
    private fun choiceOf(vararg schemas: RebarItemSchema): ItemChoice =
        ItemChoice.builder().apply {
            schemas.forEach { addFuzzy(ItemTypeWrapper(it)) }
        }.build()

    /** 按原版材质匹配的原料选择器（排除 Rebar 同名物品）。 */
    private fun vanillaChoice(material: Material): ItemChoice =
        ItemChoice.builder().addFuzzy(ItemTypeWrapper(material)).build()

    /** 注册一条无序 Rebar 配方，原料用 [ItemChoice] 直接描述。 */
    private fun registerShapeless(key: NamespacedKey, result: ItemStack, ingredients: List<ItemChoice>) {
        ShapelessRecipeType.addRecipe(
            ShapelessRebarRecipe(
                ingredients,
                FluidOrItem.Item(result),
                CraftingBookCategory.MISC,
                "",
                key
            )
        )
    }

    /**
     * 改色配方：任意颜色的同形状灯 + 目标色的荧石粉 → 目标色的该形状灯。
     *
     * 参照原版羊毛染色（`data/minecraft/recipe/dye_<color>_wool.json`）：
     * 每种目标色一条无序配方，原料是"除目标色以外的其余 17 种"灯，
     * 因此同色不会被重复染色。
     */
    private fun registerRecolorRecipes() {
        LampShape.entries.forEach { shape ->
            for (target in RECOLOR_TARGETS) {
                val illumarSchema = RebarRegistry.ITEMS[LampKeys.illumarKey(target)] ?: continue
                val resultSchema = RebarRegistry.ITEMS[shape.blockKey(target)] ?: continue

                val sourceSchemas = RECOLOR_SOURCES
                    .filter { it != target }
                    .mapNotNull { RebarRegistry.ITEMS[shape.blockKey(it)] }
                // 源物品缺失时跳过该条，避免注册出残缺配方。
                if (sourceSchemas.size != RECOLOR_SOURCES.size - 1) continue

                registerShapeless(
                    NamespacedKey(
                        RebarIlluminationAddon.instance,
                        "recolor_${shape.blockKeyPrefix}_${target.name.lowercase()}"
                    ),
                    resultSchema.createNewItemStack(),
                    listOf(choiceOf(illumarSchema), choiceOf(*sourceSchemas.toTypedArray()))
                )
            }
        }
    }

    /**
     * 洗白配方：任意有色（含彩虹）的同形状灯 + 水桶 → 无色灯。
     *
     * 水桶的原版物品属性带 `craftRemainder(BUCKET)`，合成后空桶会自动返还。
     */
    private fun registerWashRecipes() {
        LampShape.entries.forEach { shape ->
            val resultSchema = RebarRegistry.ITEMS[shape.blockKey(LampColor.COLORLESS)] ?: return@forEach

            val sourceSchemas = WASHABLE_SOURCES.mapNotNull { RebarRegistry.ITEMS[shape.blockKey(it)] }
            if (sourceSchemas.size != WASHABLE_SOURCES.size) return@forEach

            registerShapeless(
                NamespacedKey(RebarIlluminationAddon.instance, "wash_${shape.blockKeyPrefix}"),
                resultSchema.createNewItemStack(),
                listOf(vanillaChoice(Material.WATER_BUCKET), choiceOf(*sourceSchemas.toTypedArray()))
            )
        }
    }
}
