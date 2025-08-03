package ltd.mc233.itemManager.command

import ltd.mc233.itemManager.api.Regions
import ltd.mc233.itemManager.system.ItemManager
import ltd.mc233.itemManager.system.Util.regions
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

@CommandHeader("item", aliases = [""], permission = "panling.admin")
object item {
    @CommandBody(permission = "panling.admin")
    val open = mainCommand {
        execute<Player> { sender, _, _ ->
            ItemManager.open(sender)
        }
    }
}
