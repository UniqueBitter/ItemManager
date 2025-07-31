package ltd.mc233.itemManager.api


import ltd.mc233.itemManager.system.Util.base64ToItem
import ltd.mc233.itemManager.system.Util.itemToBase64
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object ItemAPI {
    fun saveItems(region: Regions, inv: Inventory) {
        // 先删除该区域的所有物品
        ItemSqlite.table.delete(ItemSqlite.dataSource) {
            where("region" eq region.id)
        }
        // 保存新的物品数据
        for (i in 0 until inv.size) {
            val item = inv.getItem(i) ?: continue
            if (item.type != Material.AIR) {
                ItemSqlite.table.insert(ItemSqlite.dataSource, "region", "slot", "item") {
                    value(region.id, i, item.itemToBase64())
                }
            }
        }
    }
    // 获取某个区域的所有物品
    fun getItems(region: Regions): MutableMap<Int, ItemStack> {
        val items = mutableMapOf<Int, ItemStack>()
        ItemSqlite.table.select(ItemSqlite.dataSource) {
            rows("slot", "item")
            where("region" eq region.id)
        }.forEach {
            val slot = getInt("slot")
            val itemBase64 = getString("item")
            try {
                items[slot] = itemBase64.base64ToItem()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return items
    }


}