package ltd.mc233.itemManager.command

import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand

@CommandHeader("test", aliases = ["朝露物品管理"], permission = "panling.admin")
object test {
    @CommandBody(permission = "panling.admin")
    val test = mainCommand {
        execute<Player> { sender, _, _ ->

        }
    }

    fun test(player: Player){

    }
}
