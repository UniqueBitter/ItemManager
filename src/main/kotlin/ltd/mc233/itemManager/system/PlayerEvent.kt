package ltd.mc233.itemManager.system

import ltd.mc233.itemManager.chest.ChestMain
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.isLeftClick
import taboolib.platform.util.isRightClick
import java.util.*

object PlayerEvent {
    private val farmList = java.util.HashMap<UUID, Int>()

    @SubscribeEvent
    fun tool(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return
        if (item.displayName == "§b朝露物品工具") {
            event.isCancelled = true
            if (event.isLeftClick()) {
                ChestMain.openchest(player)
            }
            if (event.isRightClick()) {
                ItemManager.open(player)
            }
        }
    }

}