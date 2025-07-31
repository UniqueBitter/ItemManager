package ltd.mc233.itemManager.system

import org.bukkit.NamespacedKey
import org.bukkit.event.player.PlayerDropItemEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

//禁止丢弃
object DropProtect {
    val drop = NamespacedKey("drop", "protect")

    @SubscribeEvent(EventPriority.LOWEST)
    fun drop(event: PlayerDropItemEvent) {
    }
}