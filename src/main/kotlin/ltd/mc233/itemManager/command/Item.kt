package ltd.mc233.itemManager.command

import ltd.mc233.itemManager.ItemManager
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand

@CommandHeader("item", aliases = ["朝露物品管理"], permission = "panling.admin")
object Item {
    @CommandBody(permission = "panling.admin")
    val item = mainCommand {
        execute<Player> { sender, _, _ ->
            ItemManager.open(sender)
        }
    }
}
