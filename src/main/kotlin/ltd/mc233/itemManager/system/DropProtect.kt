package ltd.mc233.itemManager.system

import org.bukkit.NamespacedKey
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.SubscribeEvent

//禁止丢弃
object DropProtect {
    val drop = NamespacedKey("drop", "protect")

    @SubscribeEvent
    fun drop(event: PlayerDropItemEvent) {
        val item = event.itemDrop.itemStack
        val meta = item.itemMeta ?: return
        // 检查物品是否有丢弃保护
        val isProtected = meta.persistentDataContainer.get(drop, PersistentDataType.BOOLEAN) ?: false
        if (isProtected) {
            event.isCancelled = true
            event.player.sendMessage("§c该物品受到丢弃保护，无法丢弃！")
        }
    }
}