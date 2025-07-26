package ltd.mc233.itemManager.api

import org.bukkit.NamespacedKey

object ItemAPI {
    val itemId = NamespacedKey("pl_att", "item_id")
    fun loadItems() {

    }

    fun saveItems() {

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


