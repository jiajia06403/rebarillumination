package io.github.rebarillumination

import io.github.pylonmc.rebar.recipe.vanilla.ShapedRecipeType
import io.github.pylonmc.rebar.registry.RebarRegistry
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe

object RebarIlluminationRecipes {

    private val colors = listOf(
        LampColor.WHITE, LampColor.ORANGE, LampColor.MAGENTA, LampColor.LIGHT_BLUE,
        LampColor.YELLOW, LampColor.LIME, LampColor.PINK, LampColor.GRAY,
        LampColor.LIGHT_GRAY, LampColor.CYAN, LampColor.PURPLE, LampColor.BLUE,
        LampColor.BROWN, LampColor.GREEN, LampColor.RED, LampColor.BLACK
    )

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
        colors.forEach { color ->
            registerIllumarRecipe(color)
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

        val recipe = ShapedRecipe(illumarKey, illumarItem)
            .shape("G", "D", "G")
            .setIngredient('G', Material.GLOWSTONE_DUST)
            .setIngredient('D', dyeMaterials[color]!!)

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val lampKey = NamespacedKey(RebarIlluminationAddon.instance, "lamp_$colorName")
        val schema = RebarRegistry.ITEMS[lampKey] ?: return
        val lampItem = schema.getItemStack()

        // 灯 = 荧光粉 + 红石灯
        val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
        val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
        val illumarItem = illumarSchema.getItemStack()

        val recipe = ShapedRecipe(lampKey, lampItem)
            .shape("I", "L")
            .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
            .setIngredient('L', Material.REDSTONE_LAMP)

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

        // 柱状灯 = 荧光粉 + 灯笼
        val recipe = ShapedRecipe(pillarLampKey, pillarLampItem)
            .shape("I", "N")
            .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
            .setIngredient('N', Material.LANTERN)

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

        // 条状灯 = 荧光粉 + 末影烛
        val recipe = ShapedRecipe(barLampKey, barLampItem)
            .shape("I", "E")
            .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
            .setIngredient('E', Material.END_ROD)

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

        // 球形灯 = 荧光粉 + 火把
        val recipe = ShapedRecipe(sphereLampKey, sphereLampItem)
            .shape("I", "T")
            .setIngredient('I', RecipeChoice.ExactChoice(illumarItem))
            .setIngredient('T', Material.TORCH)

        ShapedRecipeType.addRecipe(recipe)
    }

    private fun registerWallLampRecipe(color: LampColor) {
        val colorName = color.name.lowercase()
        val wallLampKey = NamespacedKey(RebarIlluminationAddon.instance, "wall_lamp_$colorName")
        val schema = RebarRegistry.ITEMS[wallLampKey] ?: return
        val wallLampItem = schema.getItemStack()

        val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
        val illumarSchema = RebarRegistry.ITEMS[illumarKey] ?: return
        val illumarItem = illumarSchema.getItemStack()

        // 壁灯 = 荧光粉 + 火把 + 铁锭
        val recipe = ShapedRecipe(wallLampKey, wallLampItem)
            .shape(" I ", "DIT")
            .setIngredient('I', Material.IRON_INGOT)
            .setIngredient('D', RecipeChoice.ExactChoice(illumarItem))
            .setIngredient('T', Material.TORCH)

        ShapedRecipeType.addRecipe(recipe)
    }
}
