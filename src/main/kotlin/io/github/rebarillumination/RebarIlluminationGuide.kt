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

    /** 形状 -> 其所在的指南页。 */
    private val shapePages = mapOf(
        LampShape.LAMP to lampsPage,
        LampShape.PILLAR to pillarLampsPage,
        LampShape.BAR to barLampsPage,
        LampShape.SPHERE to sphereLampsPage,
        LampShape.PATCH to wallLampsPage
    )

    fun initialize() {
        val standardColors = LampColor.entries.filter { it != LampColor.RAINBOW && it != LampColor.COLORLESS }
        val colorGroups = listOf(standardColors, listOf(LampColor.RAINBOW), listOf(LampColor.COLORLESS))

        colorGroups.forEach { colors ->
            colors.forEach { color ->
                // 无色灯没有荧石粉，对应 item 未注册，?.let 会自然跳过。
                RebarRegistry.ITEMS[LampKeys.illumarKey(color)]?.createNewItemStack()?.let { illumarPage.addItem(it) }
                LampShape.entries.forEach { shape ->
                    RebarRegistry.ITEMS[shape.blockKey(color)]?.createNewItemStack()?.let { shapePages[shape]?.addItem(it) }
                }
            }
        }

        illuminationRoot.addPage(Material.GLOWSTONE_DUST, illumarPage)
        illuminationRoot.addPage(Material.SEA_LANTERN, lampsPage)
        illuminationRoot.addPage(Material.END_ROD, pillarLampsPage)
        illuminationRoot.addPage(Material.END_ROD, barLampsPage)
        illuminationRoot.addPage(Material.LANTERN, sphereLampsPage)
        illuminationRoot.addPage(Material.TORCH, wallLampsPage)

        RebarGuide.rootPage.addPage(Material.GLOWSTONE, illuminationRoot)
    }
}
