package io.github.rebarillumination

import io.github.pylonmc.rebar.recipe.vanilla.ShapedRecipeType
import io.github.pylonmc.rebar.registry.RebarRegistry
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe

object RebarIlluminationRecipes {

    private val colors = LampColor.values().toList()

    private val dyeMaterials = mapOf(
        LampColor.WHITE to Material.WHITE_DYE,
        LampColor.ORANGE to Material.ORANGE_DYE,
        LampColor.MAGENTA to Material.MAGENTA_DYE,
        LampColor.LIGHT_BLUE to Material.LIGHT_BLUE_DYE,
        LampColor.YELLOW to Material.YELLOW_DYE,
        LampColor.LIME to Material.LIME_DYE,
        LampColor.PINK to Material.PINK_DYE,
        LampColor.GRAY to Material.GRAY_DYE,
        LampColor.LIGHT_GRAY to Material.LIGHT_GRAY_DYE,
        LampColor.CYAN to Material.CYAN_DYE,
        LampColor.PURPLE to Material.PURPLE_DYE,
        LampColor.BLUE to Material.BLUE_DYE,
        LampColor.BROWN to Material.BROWN_DYE,
        LampColor.GREEN to Material.GREEN_DYE,
        LampColor.RED to Material.RED_DYE,
        LampColor.BLACK to Material.BLACK_DYE
    )

    fun initialize() {
        // 先注册所有荧光石（包括彩虹）
        colors.forEach { color ->
            if (color != LampColor.RAINBOW) {
                registerIllumarRecipe(color)
            }
        }
        registerRainbowIllumarRecipe()

        // 然后注册所有灯配方（包括彩虹）
        colors.forEach { color ->
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
            .setIngredient('D', dyeMaterials[color]!!)

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val lampKey = NamespacedKey(RebarIlluminationAddon.instance, "lamp_$colorName")
        val schema = RebarRegistry.ITEMS[lampKey] ?: return
        val lampItem = schema.getItemStack()

        val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
        val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
        val illumarItem = illumarSchema.getItemStack()

        val recipe = ShapedRecipe(lampKey, lampItem)
            .shape("NNN", "NIN", "NNN")
            .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
            .setIngredient('N', RecipeChoice.MaterialChoice(
                Material.IRON_NUGGET,
                Material.GOLD_NUGGET,
                Material.COPPER_NUGGET
            ))

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerPillarLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val pillarLampKey = NamespacedKey(RebarIlluminationAddon.instance, "pillar_lamp_$colorName")
        val schema = RebarRegistry.ITEMS[pillarLampKey] ?: return
        val pillarLampItem = schema.getItemStack()

        val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
        val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
        val illumarItem = illumarSchema.getItemStack()

        val recipe = ShapedRecipe(pillarLampKey, pillarLampItem)
            .shape(" N ", " I ", " N ")
            .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
            .setIngredient('N', RecipeChoice.MaterialChoice(
                Material.IRON_NUGGET,
                Material.GOLD_NUGGET,
                Material.COPPER_NUGGET
            ))

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerBarLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val barLampKey = NamespacedKey(RebarIlluminationAddon.instance, "bar_lamp_$colorName")
        val schema = RebarRegistry.ITEMS[barLampKey] ?: return
        val barLampItem = schema.getItemStack()

        val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
        val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
        val illumarItem = illumarSchema.getItemStack()

        val recipe = ShapedRecipe(barLampKey, barLampItem)
            .shape("NIN")
            .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
            .setIngredient('N', RecipeChoice.MaterialChoice(
                Material.IRON_NUGGET,
                Material.GOLD_NUGGET,
                Material.COPPER_NUGGET
            ))

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerSphereLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val sphereLampKey = NamespacedKey(RebarIlluminationAddon.instance, "sphere_lamp_$colorName")
        val schema = RebarRegistry.ITEMS[sphereLampKey] ?: return
        val sphereLampItem = schema.getItemStack()

        val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
        val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
        val illumarItem = illumarSchema.getItemStack()

        val recipe = ShapedRecipe(sphereLampKey, sphereLampItem)
            .shape(" N ", "NIN", " N ")
            .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
            .setIngredient('N', RecipeChoice.MaterialChoice(
                Material.IRON_NUGGET,
                Material.GOLD_NUGGET,
                Material.COPPER_NUGGET
            ))

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerWallLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val wallLampKey = NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_$colorName")
        val schema = RebarRegistry.ITEMS[wallLampKey] ?: return
        val wallLampItem = schema.getItemStack()

        val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
        val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
        val illumarItem = illumarSchema.getItemStack()

        // 有序配方：3个任意粒 + 1个荧光石（类似原版命名牌配方）
        val recipe = ShapedRecipe(NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_${colorName}_top"), wallLampItem.clone())
            .shape("NNN", " I ")
            .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
            .setIngredient('N', RecipeChoice.MaterialChoice(
                Material.IRON_NUGGET,
                Material.GOLD_NUGGET,
                Material.COPPER_NUGGET
            ))
        ShapedRecipeType.addRecipe(recipe)

        // 荧光石在上方的变体
        val recipeTop = ShapedRecipe(NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_${colorName}_center"), wallLampItem.clone())
            .shape(" I ", "NNN")
            .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
            .setIngredient('N', RecipeChoice.MaterialChoice(
                Material.IRON_NUGGET,
                Material.GOLD_NUGGET,
                Material.COPPER_NUGGET
            ))
        ShapedRecipeType.addRecipe(recipeTop)
    }

    private fun registerRainbowIllumarRecipe() {
        val rainbowIllumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_rainbow")
        val schema = RebarRegistry.ITEMS[rainbowIllumarKey] ?: return
        val rainbowIllumarItem = schema.getItemStack()

        // 获取所有荧光石物品
        val allIllumarItems = colors.mapNotNull { color ->
            val key = NamespacedKey(RebarIlluminationAddon.instance, "illumar_${color.name.lowercase()}")
            RebarRegistry.ITEMS[key]?.getItemStack()
        }

        if (allIllumarItems.size < 9) return

        val recipe = ShapedRecipe(rainbowIllumarKey, rainbowIllumarItem)
            .shape("III", "III", "III")
            .setIngredient('I', RecipeChoice.ExactChoice(allIllumarItems))

        ShapedRecipeType.addRecipe(recipe)
    }
}
