package ltd.mc233.itemManager

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.buildItem

object Manager {

    fun open(player: Player) {
        player.openMenu<Basic>("§b 朝露物品管理") {
            map(
                "#########",
                "0 1 2 3 4",
                "         ",
                "5 6 7 8 9",
                "#########",
            )
            set('#',ItemCache.bar.clone()){
                isCancelled = true
            }
            set('0', buildItem(XMaterial.JUNGLE_SAPLING) {
                name = "§c任务物品"
                flags.add(ItemFlag.HIDE_ATTRIBUTES)
            }) {
            }
            set('1', buildItem(XMaterial.JUNGLE_SAPLING) {
                name = "§a食物"
                flags.add(ItemFlag.HIDE_ATTRIBUTES)
            }) {
            }
            set('2', buildItem(XMaterial.JUNGLE_SAPLING) {
                name = "§b元素"
                flags.add(ItemFlag.HIDE_ATTRIBUTES)
            }) {
            }
            set('3', buildItem(XMaterial.JUNGLE_SAPLING) {
                name = "§6装备原核"
                flags.add(ItemFlag.HIDE_ATTRIBUTES)
            }) {
            }
            set('4', buildItem(XMaterial.JUNGLE_SAPLING) {
                name = "§f怪物掉落物"
                flags.add(ItemFlag.HIDE_ATTRIBUTES)
            }) {
            }
            set('5', buildItem(XMaterial.JUNGLE_SAPLING) {
                name = "§a采集物"
                flags.add(ItemFlag.HIDE_ATTRIBUTES)
            }) {
            }
            set('6', buildItem(XMaterial.JUNGLE_SAPLING) {
                name = "§c未解封物品"
                flags.add(ItemFlag.HIDE_ATTRIBUTES)
            }) {
            }
            set('9', buildItem(XMaterial.JUNGLE_SAPLING) {
                name = "§b常用"
                flags.add(ItemFlag.HIDE_ATTRIBUTES)
            }) {
            }
        }
    }
}