package ltd.mc233.itemManager

import org.bukkit.Material
import taboolib.platform.util.buildItem

object ItemCache {
    val lastPage = buildItem(Material.BLACK_STAINED_GLASS_PANE) {
        name = "§b上一页"
        customModelData = 5
    }
    val nextPage = buildItem(Material.BLACK_STAINED_GLASS_PANE) {
        name = "§b下一页"
        customModelData = 6
    }
    val lastPage2 = buildItem(Material.WOODEN_AXE) {
        name = "§b没有上一页"
    }
    val nextPage2 = buildItem(Material.WOODEN_AXE) {
        name = "§b没有下一页"
    }

    val bar = buildItem(Material.WHITE_STAINED_GLASS_PANE) { name = " " }

}