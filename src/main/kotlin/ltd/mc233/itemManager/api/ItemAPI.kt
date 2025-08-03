package ltd.mc233.itemManager.api

import ltd.mc233.itemManager.system.Recipe
import ltd.mc233.itemManager.system.Util.base64ToItem
import ltd.mc233.itemManager.system.Util.itemToBase64
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

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

    // 根据物品ID获取物品模板
    fun getItemById(itemId: String): ItemStack? {
        // 遍历所有区域查找具有指定ID的物品
        for (region in Regions.values()) {
            val items = getItems(region)
            for (item in items.values) {
                val storedId = item.itemMeta?.persistentDataContainer?.get(
                    Recipe.itemId,
                    PersistentDataType.STRING
                )
                if (storedId == itemId) {
                    return item.clone()
                }
            }
        }
        return null
    }

    // 给予玩家指定ID的物品
    fun giveItem(player: Player, itemId: String, amount: Int = 1): Boolean {
        val template = getItemById(itemId)
        if (template == null) {
            player.sendMessage("§c未找到ID为 '$itemId' 的物品!")
            return false
        }

        val giveItem = template.clone()
        giveItem.amount = amount

        val leftOver = player.inventory.addItem(giveItem)
        if (leftOver.isNotEmpty()) {
            // 背包满了，掉落到地上
            leftOver.values.forEach {
                player.world.dropItem(player.location, it)
            }
            player.sendMessage("§e背包已满，部分物品掉落在地上!")
        }

        player.sendMessage("§a成功获得 §f${giveItem.itemMeta?.displayName ?: giveItem.type.name} §ax$amount")
        return true
    }

    // 检测玩家背包中是否有指定ID的物品
    fun hasItem(player: Player, itemId: String): Boolean {
        return player.inventory.contents.any { item ->
            item?.itemMeta?.persistentDataContainer?.get(
                Recipe.itemId,
                PersistentDataType.STRING
            ) == itemId
        }
    }

    // 获取玩家拥有的指定ID物品数量
    fun getItemCount(player: Player, itemId: String): Int {
        return player.inventory.contents.sumOf { item ->
            if (item?.itemMeta?.persistentDataContainer?.get(
                    Recipe.itemId,
                    PersistentDataType.STRING
                ) == itemId) {
                item.amount
            } else 0
        }
    }

    // 检测玩家是否拥有足够数量的指定ID物品
    fun hasItem(player: Player, itemId: String, requiredAmount: Int): Boolean {
        return getItemCount(player, itemId) >= requiredAmount
    }

    // 扣除玩家指定ID的物品
    fun takeItem(player: Player, itemId: String, amount: Int = 1): Boolean {
        val currentAmount = getItemCount(player, itemId)
        if (currentAmount < amount) {
            return false
        }

        var remainingToTake = amount
        val inventory = player.inventory

        for (i in 0 until inventory.size) {
            val item = inventory.getItem(i) ?: continue
            val storedId = item.itemMeta?.persistentDataContainer?.get(
                Recipe.itemId,
                PersistentDataType.STRING
            )

            if (storedId == itemId) {
                if (item.amount <= remainingToTake) {
                    remainingToTake -= item.amount
                    inventory.setItem(i, null)
                } else {
                    item.amount -= remainingToTake
                    remainingToTake = 0
                }

                if (remainingToTake <= 0) break
            }
        }

        return true
    }

    // 获取所有已注册的物品ID列表
    fun getAllItemIds(): List<String> {
        val itemIds = mutableListOf<String>()
        for (region in Regions.values()) {
            val items = getItems(region)
            for (item in items.values) {
                val storedId = item.itemMeta?.persistentDataContainer?.get(
                    Recipe.itemId,
                    PersistentDataType.STRING
                )
                if (storedId != null && !itemIds.contains(storedId)) {
                    itemIds.add(storedId)
                }
            }
        }
        return itemIds.sorted()
    }

    // 检查物品ID是否存在
    fun hasItemId(itemId: String): Boolean {
        return getItemById(itemId) != null
    }

    // 获取玩家背包中所有指定ID的物品槽位
    fun getItemSlots(player: Player, itemId: String): List<Int> {
        val slots = mutableListOf<Int>()
        player.inventory.contents.forEachIndexed { index, item ->
            if (item?.itemMeta?.persistentDataContainer?.get(
                    Recipe.itemId,
                    PersistentDataType.STRING
                ) == itemId) {
                slots.add(index)
            }
        }
        return slots
    }

    // 批量给予物品
    fun giveItems(player: Player, items: Map<String, Int>): Int {
        var successCount = 0
        items.forEach { (itemId, amount) ->
            if (giveItem(player, itemId, amount)) {
                successCount++
            }
        }
        return successCount
    }

    // 批量扣除物品
    fun takeItems(player: Player, items: Map<String, Int>): Boolean {
        // 先检查是否都有足够的物品
        for ((itemId, amount) in items) {
            if (!hasItem(player, itemId, amount)) {
                return false
            }
        }

        // 如果都有足够的物品，则扣除
        items.forEach { (itemId, amount) ->
            takeItem(player, itemId, amount)
        }
        return true
    }
}