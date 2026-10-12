package io.github.rebarillumination

import io.github.pylonmc.rebar.recipe.vanilla.ShapedRecipeType
import io.github.pylonmc.rebar.recipe.vanilla.ShapelessRecipeType
import io.github.pylonmc.rebar.recipe.vanilla.ShapedRebarRecipe
import io.github.pylonmc.rebar.recipe.vanilla.ShapelessRebarRecipe
import io.github.pylonmc.rebar.registry.RebarRegistry
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe

object RebarIlluminationRecipes {

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
        val recipe = org.bukkit.inventory.ShapelessRecipe(rainbowIllumarKey, rainbowIllumarItem)
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
                .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
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
                .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
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
                .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
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
                .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
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
            ).shape("NNN", " I ").setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.COPPER_NUGGET
                ))
            val center = ShapedRecipe(
                NamespacedKey(RebarIlluminationAddon.instance, "${LampShape.PATCH.blockKeyPrefix}_${suffix}_center"),
                wallLampItem.clone()
            ).shape(" I ", "NNN").setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
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
    private fun illumarItemOf(color: LampColor): org.bukkit.inventory.ItemStack? {
        return RebarRegistry.ITEMS[LampKeys.illumarKey(color)]?.createNewItemStack()
    }
}
