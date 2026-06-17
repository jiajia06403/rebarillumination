package io.github.rebarillumination

import io.github.pylonmc.rebar.recipe.vanilla.ShapedRecipeType
import io.github.pylonmc.rebar.registry.RebarRegistry
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe

object RebarIlluminationRecipes {

    fun initialize() {
        // 注册荧石粉配方（不包括彩虹和无色）
        LampColor.entries.filter { it.hasIllumar && it != LampColor.RAINBOW }.forEach { color ->
            registerIllumarRecipe(color)
        }

        // 注册彩虹荧石粉配方
        registerRainbowIllumarRecipe()

        // 注册所有灯配方
        LampColor.entries.forEach { color ->
            registerLampRecipe(color)
            registerPillarLampRecipe(color)
            registerBarLampRecipe(color)
            registerSphereLampRecipe(color)
            registerWallLampRecipe(color)
        }
    }

    private fun registerIllumarRecipe(color: LampColor) {
        val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_${color.name.lowercase()}")
        val schema = RebarRegistry.ITEMS[illumarKey] ?: return
        val illumarItem = schema.getItemStack()
        illumarItem.amount = 8

        val recipe = ShapedRecipe(illumarKey, illumarItem)
            .shape("GGG", "GDG", "GGG")
            .setIngredient('G', Material.GLOWSTONE_DUST)
            .setIngredient('D', color.dyeMaterial)

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerRainbowIllumarRecipe() {
        val rainbowIllumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_rainbow")
        val schema = RebarRegistry.ITEMS[rainbowIllumarKey] ?: return
        val rainbowIllumarItem = schema.getItemStack()

        // 获取所有荧石粉物品（不包括彩虹和无色）
        val allIllumarItems = LampColor.entries
            .filter { it.hasIllumar && it != LampColor.RAINBOW }
            .mapNotNull { color ->
                val key = NamespacedKey(RebarIlluminationAddon.instance, "illumar_${color.name.lowercase()}")
                RebarRegistry.ITEMS[key]?.getItemStack()
            }

        if (allIllumarItems.size < 9) return

        val recipe = ShapedRecipe(rainbowIllumarKey, rainbowIllumarItem)
            .shape("III", "III", "III")
            .setIngredient('I', RecipeChoice.ExactChoice(allIllumarItems))

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val lampKey = NamespacedKey(RebarIlluminationAddon.instance, "lamp_$colorName")
        val schema = RebarRegistry.ITEMS[lampKey] ?: return
        val lampItem = schema.getItemStack()

        val recipe = if (color.hasIllumar) {
            // 有荧石粉的颜色：使用荧石粉
            val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
            val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
            val illumarItem = illumarSchema.getItemStack()

            ShapedRecipe(lampKey, lampItem)
                .shape("NNN", "NIN", "NNN")
                .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
        } else {
            // 无色：直接使用原版荧石粉
            ShapedRecipe(lampKey, lampItem)
                .shape("NNN", "NGN", "NNN")
                .setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
        }

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerPillarLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val pillarLampKey = NamespacedKey(RebarIlluminationAddon.instance, "pillar_lamp_$colorName")
        val schema = RebarRegistry.ITEMS[pillarLampKey] ?: return
        val pillarLampItem = schema.getItemStack()

        val recipe = if (color.hasIllumar) {
            val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
            val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
            val illumarItem = illumarSchema.getItemStack()

            ShapedRecipe(pillarLampKey, pillarLampItem)
                .shape(" N ", " I ", " N ")
                .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
        } else {
            ShapedRecipe(pillarLampKey, pillarLampItem)
                .shape(" N ", " G ", " N ")
                .setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
        }

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerBarLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val barLampKey = NamespacedKey(RebarIlluminationAddon.instance, "bar_lamp_$colorName")
        val schema = RebarRegistry.ITEMS[barLampKey] ?: return
        val barLampItem = schema.getItemStack()

        val recipe = if (color.hasIllumar) {
            val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
            val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
            val illumarItem = illumarSchema.getItemStack()

            ShapedRecipe(barLampKey, barLampItem)
                .shape("NIN")
                .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
        } else {
            ShapedRecipe(barLampKey, barLampItem)
                .shape("NGN")
                .setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
        }

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerSphereLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val sphereLampKey = NamespacedKey(RebarIlluminationAddon.instance, "sphere_lamp_$colorName")
        val schema = RebarRegistry.ITEMS[sphereLampKey] ?: return
        val sphereLampItem = schema.getItemStack()

        val recipe = if (color.hasIllumar) {
            val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
            val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
            val illumarItem = illumarSchema.getItemStack()

            ShapedRecipe(sphereLampKey, sphereLampItem)
                .shape(" N ", "NIN", " N ")
                .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
        } else {
            ShapedRecipe(sphereLampKey, sphereLampItem)
                .shape(" N ", "NGN", " N ")
                .setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
        }

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerWallLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val wallLampKey = NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_$colorName")
        val schema = RebarRegistry.ITEMS[wallLampKey] ?: return
        val wallLampItem = schema.getItemStack()

        if (color.hasIllumar) {
            val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
            val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
            val illumarItem = illumarSchema.getItemStack()

            // 有序配方：3个任意粒 + 1个荧石粉
            val recipe = ShapedRecipe(NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_${colorName}_top"), wallLampItem.clone())
                .shape("NNN", " I ")
                .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
            ShapedRecipeType.addRecipe(recipe)

            val recipeTop = ShapedRecipe(NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_${colorName}_center"), wallLampItem.clone())
                .shape(" I ", "NNN")
                .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
            ShapedRecipeType.addRecipe(recipeTop)
        } else {
            // 无色：直接使用原版荧石粉
            val recipe = ShapedRecipe(NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_${colorName}_top"), wallLampItem.clone())
                .shape("NNN", " G ")
                .setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
            ShapedRecipeType.addRecipe(recipe)

            val recipeTop = ShapedRecipe(NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_${colorName}_center"), wallLampItem.clone())
                .shape(" G ", "NNN")
                .setIngredient('G', Material.GLOWSTONE_DUST)
                .setIngredient('N', RecipeChoice.MaterialChoice(
                    Material.IRON_NUGGET,
                    Material.GOLD_NUGGET,
                    Material.COPPER_NUGGET
                ))
            ShapedRecipeType.addRecipe(recipeTop)
        }
    }
}
