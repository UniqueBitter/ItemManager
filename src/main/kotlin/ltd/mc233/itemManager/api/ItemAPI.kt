package ltd.mc233.itemManager.api

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType


object ItemAPI {
    val itemDatas = HashSet<ItemData>(1000)
    val items = HashSet<ItemStack>(1000)
    val itemMap = HashMap<String, ItemStack>(1000)
    val itemId = NamespacedKey("pl_att", "item_id")
    fun loadItems() {
        ItemSQLite.getItems()
        itemDatas.forEach {
            items.add(it.item)
            val id = it.item.persistentDataContainer.get(itemId, PersistentDataType.STRING)
                ?: return@forEach
            itemMap[id] = it.item.clone()
        }
    }

    fun saveItems() {
        items.clear()
        itemDatas.forEach {
            items.add(it.item)
        }
        ItemSQLite.saveItems(itemDatas)
    }
}


//┌─────────────────┐
//│   玩家界面GUI    │  ← 玩家看到的物品展示界面
//├─────────────────┤
//│   业务逻辑层     │  ← 处理物品加载、保存、分类等逻辑
//├─────────────────┤
//│   数据库操作层   │  ← 负责与SQLite数据库交互
//├─────────────────┤
//│   SQLite数据库   │  ← 持久化存储物品数据
//└─────────────────┘


