package ltd.mc233.itemManager.system

import ltd.mc233.itemManager.api.Regions
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.buildItem
import java.util.*

object ItemManager {
    val page = HashMap<UUID, Int>()
    val region = NamespacedKey("pl_att", "region")
    fun open(player: Player) {
        player.openMenu<Basic>("§b 物品管理") {
            map(
                "#########",
                "0 1 2 3 4",
                "         ",
                "5 6 7 8 9",
                "#########",
            )
            set('#', ItemCache.bar.clone()) {
                isCancelled = true
            }
            set('0', buildItem(XMaterial.WRITTEN_BOOK) {
                name = "§c任务物品"
                hideAll()
            }) {
                Screen.open(player, Regions.QUEST_ITEMS)
            }
            set('1', buildItem(XMaterial.ENCHANTED_GOLDEN_APPLE) {
                name = "§a食物"
                hideAll()
            }) {
                Screen.open(player, Regions.FOOD)
            }
            set('2', buildItem(XMaterial.STRING) {
                name = "§b元素"
                hideAll()
            }) {
                Screen.open(player, Regions.ELEMENTS)
            }
            set('3', buildItem(XMaterial.NAME_TAG) {
                name = "§6装备原核"
                hideAll()
            }) {
                Screen.open(player, Regions.EQUIPMENT_CORE)
            }
            set('4', buildItem(XMaterial.SPIDER_EYE) {
                name = "§f怪物掉落物"
                hideAll()
            }) {
                Screen.open(player, Regions.MONSTER_DROPS)
            }
            set('5', buildItem(XMaterial.PRISMARINE_CRYSTALS) {
                name = "§a采集物"
                hideAll()
            }) {
                Screen.open(player, Regions.COLLECTION_ITEMS)
            }
            set('6', buildItem(XMaterial.SUGAR) {
                name = "§c未解封物品"
                shiny()
                hideAll()
            }) {
                Screen.open(player, Regions.UNSEALED_ITEMS)
            }
            set('9', buildItem(XMaterial.YELLOW_DYE) {
                name = "§b常用"
                shiny()
                hideAll()
            }) {
                Screen.open(player, Regions.COMMON_ITEMS)
            }
        }
    }
}