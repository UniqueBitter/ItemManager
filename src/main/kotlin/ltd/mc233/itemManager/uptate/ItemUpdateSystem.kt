package ltd.mc233.itemManager.system

import ltd.mc233.itemManager.api.ItemAPI
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendLang
import java.util.*

object ItemUpdateSystem {

    // 玩家更新冷却 (30秒内不重复更新同一玩家)
    private val cooldowns = mutableMapOf<UUID, Long>()

    // 静默更新标记
    private val silentUpdates = mutableSetOf<UUID>()

    // 更新单个物品 - 保持原有数量
    private fun ItemStack.updateToLatest(): ItemStack {
        val customId = itemMeta?.persistentDataContainer?.get(Recipe.itemId, PersistentDataType.STRING)
            ?: return this

        val latest = ItemAPI.getItemById(customId) ?: return this
        latest.amount = this.amount // 保持原有数量
        return latest
    }

    // 静默更新玩家物品
    private fun Player.updateItemsSilently() {
        val now = System.currentTimeMillis()
        val lastUpdate = cooldowns[uniqueId] ?: 0
        if (now - lastUpdate < 30000) return // 30秒冷却

        submit(delay = 1) {
            var count = 0
            inventory.contents.forEachIndexed { index, item ->
                if (item?.hasCustomId() == true) {
                    val updated = item.updateToLatest()
                    if (updated != item) {
                        inventory.setItem(index, updated)
                        count++
                    }
                }
            }

            // 只在非静默模式下发送消息
            if (count > 0 && uniqueId !in silentUpdates) {
                sendMessage("§a物品已自动更新！")
            }

            if (count > 0) {
                cooldowns[uniqueId] = now
            }

            // 清除静默标记
            silentUpdates.remove(uniqueId)
        }
    }

    private fun ItemStack.hasCustomId(): Boolean {
        return itemMeta?.persistentDataContainer?.get(Recipe.itemId, PersistentDataType.STRING) != null
    }

    // 强制更新 (无冷却，无消息)
    fun forceUpdate(player: Player, silent: Boolean = false) {
        cooldowns.remove(player.uniqueId)
        if (silent) {
            silentUpdates.add(player.uniqueId)
        }
        player.updateItemsSilently()
    }

    // 监听事件 - 只在真正需要的时候触发
    @SubscribeEvent
    fun onJoin(event: org.bukkit.event.player.PlayerJoinEvent) {
        // 登录时静默更新，延迟3秒避免干扰登录流程
        submit(delay = 60) {
            forceUpdate(event.player, silent = true)
        }
    }

    @SubscribeEvent
    fun onInventoryClick(event: org.bukkit.event.inventory.InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val item = event.currentItem ?: return
        // 检查是否是自定义物品
        val customId = item.itemMeta?.persistentDataContainer?.get(
            Recipe.itemId, PersistentDataType.STRING
        ) ?: return
        // 延迟更新
        submit(delay = 1) {
            player.updateItems(silent = true)
        }
    }

    // 当玩家空闲时检查更新（每5分钟检查一次）
    init {
        submit(async = true, delay = 6000, period = 6000) { // 5分钟
            org.bukkit.Bukkit.getOnlinePlayers()
                .filter { player ->
                    // 只更新那些背包里有自定义物品的玩家
                    player.inventory.contents.any { it?.hasCustomId() == true }
                }
                .forEach { player ->
                    forceUpdate(player, silent = true)
                }
        }
    }
}

// 扩展函数
fun Player.updateItems(silent: Boolean = false) = ItemUpdateSystem.forceUpdate(this, silent)

// 当物品被修改时调用 - 立即推送更新
fun notifyItemUpdated(itemId: String) {
    submit {
        org.bukkit.Bukkit.getOnlinePlayers()
            .filter { player ->
                player.inventory.contents.any { item ->
                    item?.itemMeta?.persistentDataContainer?.get(Recipe.itemId, PersistentDataType.STRING) == itemId
                }
            }
            .forEach {
                it.updateItems(silent = false) // 管理员更新时通知玩家
            }
    }
}

// 批量通知更新
fun notifyItemsUpdated(itemIds: List<String>) {
    submit {
        org.bukkit.Bukkit.getOnlinePlayers()
            .filter { player ->
                player.inventory.contents.any { item ->
                    val id = item?.itemMeta?.persistentDataContainer?.get(Recipe.itemId, PersistentDataType.STRING)
                    id in itemIds
                }
            }
            .forEach {
                it.updateItems(silent = false)
            }
    }
}

// 全服静默更新所有物品
fun updateAllPlayersItems(silent: Boolean = true) {
    submit {
        org.bukkit.Bukkit.getOnlinePlayers().forEach {
            it.updateItems(silent = silent)
        }
    }
}