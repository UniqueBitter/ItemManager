package ltd.mc233.itemManager.command

import ltd.mc233.itemManager.api.Regions
import ltd.mc233.itemManager.system.Util.regions
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

@CommandHeader("givetool", aliases = [""], permission = "panling.admin")
object givetool {
    @CommandBody(permission = "panling.admin")
    val givetool = mainCommand {
        execute<Player> { sender, _, _ ->
            val item = buildItem(XMaterial.REDSTONE_ORE){
                name = "§b朝露物品工具"
                lore += "§7左键打开缓存箱子"
                lore += "§7右键打开物品管理面板"
            }
            item.regions = Regions.COMMON_ITEMS
                sender.give(item)
        }
    }
}
