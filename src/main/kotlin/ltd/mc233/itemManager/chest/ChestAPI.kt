package ltd.mc233.itemManager.chest

import ltd.mc233.itemManager.system.Util.base64ToItem
import ltd.mc233.itemManager.system.Util.itemToBase64
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object ChestAPI {
    fun saveitem(player: Player, inv: Inventory) {
        ChestSqlite.table.delete(ChestSqlite.dataSource) {
            where { "player" eq player.name }
        }
        for (i in 0 until inv.size) {
            val item = inv.getItem(i) ?: continue
            if (item.type != Material.AIR) {
                ChestSqlite.table.insert(ChestSqlite.dataSource, "player", "slot", "item") {
                    value(player.name, i, item.itemToBase64())
                }
            }
        }
    }
    fun getitem(player: Player): MutableMap<Int, ItemStack> {
        val items = mutableMapOf<Int, ItemStack>()
        ChestSqlite.table.select(ChestSqlite.dataSource) {
            rows("slot", "item")
            where { "player" eq player.name }
        }.forEach {
            val slot = getInt("slot")
            val itembase64 = getString("item")
            try {
                items[slot] = itembase64.base64ToItem()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return items
    }
}