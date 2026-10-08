package io.github.rebarillumination

import io.github.pylonmc.rebar.content.guide.RebarGuide
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.rebar.registry.RebarRegistry
import io.github.rebarillumination.util.LampColor
import org.bukkit.Material
import org.bukkit.NamespacedKey

object RebarIlluminationGuide {

    private val illuminationRoot = SimpleStaticGuidePage(NamespacedKey(RebarIlluminationAddon.instance, "illumination"))
    private val lampsPage = SimpleStaticGuidePage(NamespacedKey(RebarIlluminationAddon.instance, "illumination_lamps"))
    private val pillarLampsPage = SimpleStaticGuidePage(NamespacedKey(RebarIlluminationAddon.instance, "illumination_pillar_lamps"))
    private val barLampsPage = SimpleStaticGuidePage(NamespacedKey(RebarIlluminationAddon.instance, "illumination_bar_lamps"))
    private val sphereLampsPage = SimpleStaticGuidePage(NamespacedKey(RebarIlluminationAddon.instance, "illumination_sphere_lamps"))
    private val wallLampsPage = SimpleStaticGuidePage(NamespacedKey(RebarIlluminationAddon.instance, "illumination_patch_lamps"))
    private val illumarPage = SimpleStaticGuidePage(NamespacedKey(RebarIlluminationAddon.instance, "illumination_illumar"))

    fun initialize() {
        val colors = listOf(
            LampColor.WHITE, LampColor.ORANGE, LampColor.MAGENTA, LampColor.LIGHT_BLUE,
            LampColor.YELLOW, LampColor.LIME, LampColor.PINK, LampColor.GRAY,
            LampColor.LIGHT_GRAY, LampColor.CYAN, LampColor.PURPLE, LampColor.BLUE,
            LampColor.BROWN, LampColor.GREEN, LampColor.RED, LampColor.BLACK
        )

        colors.forEach { color ->
            val colorName = color.name.lowercase()
            
            val illumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$colorName")
            val lampKey = NamespacedKey(RebarIlluminationAddon.instance, "lamp_$colorName")
            val pillarLampKey = NamespacedKey(RebarIlluminationAddon.instance, "pillar_lamp_$colorName")
            val barLampKey = NamespacedKey(RebarIlluminationAddon.instance, "bar_lamp_$colorName")
            val sphereLampKey = NamespacedKey(RebarIlluminationAddon.instance, "sphere_lamp_$colorName")
            val wallLampKey = NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_$colorName")

            RebarRegistry.ITEMS[illumarKey]?.createNewItemStack()?.let { illumarPage.addItem(it) }
            RebarRegistry.ITEMS[lampKey]?.createNewItemStack()?.let { lampsPage.addItem(it) }
            RebarRegistry.ITEMS[pillarLampKey]?.createNewItemStack()?.let { pillarLampsPage.addItem(it) }
            RebarRegistry.ITEMS[barLampKey]?.createNewItemStack()?.let { barLampsPage.addItem(it) }
            RebarRegistry.ITEMS[sphereLampKey]?.createNewItemStack()?.let { sphereLampsPage.addItem(it) }
            RebarRegistry.ITEMS[wallLampKey]?.createNewItemStack()?.let { wallLampsPage.addItem(it) }
        }

        val rainbowKey = "rainbow"
        val rainbowIllumarKey = NamespacedKey(RebarIlluminationAddon.instance, "illumar_$rainbowKey")
        val rainbowLampKey = NamespacedKey(RebarIlluminationAddon.instance, "lamp_$rainbowKey")
        val rainbowPillarLampKey = NamespacedKey(RebarIlluminationAddon.instance, "pillar_lamp_$rainbowKey")
        val rainbowBarLampKey = NamespacedKey(RebarIlluminationAddon.instance, "bar_lamp_$rainbowKey")
        val rainbowSphereLampKey = NamespacedKey(RebarIlluminationAddon.instance, "sphere_lamp_$rainbowKey")
        val rainbowWallLampKey = NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_$rainbowKey")

        RebarRegistry.ITEMS[rainbowIllumarKey]?.createNewItemStack()?.let { illumarPage.addItem(it) }
        RebarRegistry.ITEMS[rainbowLampKey]?.createNewItemStack()?.let { lampsPage.addItem(it) }
        RebarRegistry.ITEMS[rainbowPillarLampKey]?.createNewItemStack()?.let { pillarLampsPage.addItem(it) }
        RebarRegistry.ITEMS[rainbowBarLampKey]?.createNewItemStack()?.let { barLampsPage.addItem(it) }
        RebarRegistry.ITEMS[rainbowSphereLampKey]?.createNewItemStack()?.let { sphereLampsPage.addItem(it) }
        RebarRegistry.ITEMS[rainbowWallLampKey]?.createNewItemStack()?.let { wallLampsPage.addItem(it) }

        val colorlessKey = "colorless"
        val colorlessLampKey = NamespacedKey(RebarIlluminationAddon.instance, "lamp_$colorlessKey")
        val colorlessPillarLampKey = NamespacedKey(RebarIlluminationAddon.instance, "pillar_lamp_$colorlessKey")
        val colorlessBarLampKey = NamespacedKey(RebarIlluminationAddon.instance, "bar_lamp_$colorlessKey")
        val colorlessSphereLampKey = NamespacedKey(RebarIlluminationAddon.instance, "sphere_lamp_$colorlessKey")
        val colorlessWallLampKey = NamespacedKey(RebarIlluminationAddon.instance, "patch_lamp_$colorlessKey")

        RebarRegistry.ITEMS[colorlessLampKey]?.createNewItemStack()?.let { lampsPage.addItem(it) }
        RebarRegistry.ITEMS[colorlessPillarLampKey]?.createNewItemStack()?.let { pillarLampsPage.addItem(it) }
        RebarRegistry.ITEMS[colorlessBarLampKey]?.createNewItemStack()?.let { barLampsPage.addItem(it) }
        RebarRegistry.ITEMS[colorlessSphereLampKey]?.createNewItemStack()?.let { sphereLampsPage.addItem(it) }
        RebarRegistry.ITEMS[colorlessWallLampKey]?.createNewItemStack()?.let { wallLampsPage.addItem(it) }

        illuminationRoot.addPage(Material.GLOWSTONE_DUST, illumarPage)
        illuminationRoot.addPage(Material.SEA_LANTERN, lampsPage)
        illuminationRoot.addPage(Material.END_ROD, pillarLampsPage)
        illuminationRoot.addPage(Material.END_ROD, barLampsPage)
        illuminationRoot.addPage(Material.LANTERN, sphereLampsPage)
        illuminationRoot.addPage(Material.TORCH, wallLampsPage)

        RebarGuide.rootPage.addPage(Material.GLOWSTONE, illuminationRoot)
    }
}
