package ltd.mc233.itemManager.chest

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

@CommandHeader("ubchest", aliases = ["朝露箱子"], permission = "panling.admin")
object ChestMain {
    //学习数据库
    @CommandBody(permission = "panling.admin")
    val ubchest = mainCommand {
        execute<Player> { sender, _, _ ->
            openchest( sender)
        }
    }

    fun openchest(player: Player) {
        val items = ChestAPI.getitem(player)
        val inv = Bukkit.createInventory(null, 54, "§b朝露箱子")
        items.forEach { (slot, item) ->
            inv.setItem(slot, item)
        }
        player.openInventory(inv)
    }

    @SubscribeEvent
    fun onChest(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        if (event.view.title != "§b朝露箱子") return
        submit(delay = 1) { ChestAPI.saveitem(player, event.inventory) }
    }

    @SubscribeEvent
    fun onChest(event: InventoryDragEvent) {
        val player = event.whoClicked as Player
        if (event.view.title != "§b朝露箱子") return
        submit(delay = 1) { ChestAPI.saveitem(player, event.inventory) }
    }

    @SubscribeEvent
    fun onChest(event: InventoryCloseEvent) {
        val player = event.player as Player
        if (event.view.title != "§b朝露箱子") return
        ChestAPI.saveitem(player, event.inventory)
    }
}


