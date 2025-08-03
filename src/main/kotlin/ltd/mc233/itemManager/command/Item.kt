package ltd.mc233.itemManager.command

import ltd.mc233.itemManager.api.ItemAPI
import ltd.mc233.itemManager.api.Regions
import ltd.mc233.itemManager.system.Util.regions
import ltd.mc233.itemManager.system.updateAllPlayersItems
import ltd.mc233.itemManager.system.notifyItemUpdated
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

@CommandHeader("ubitem", aliases = [""], permission = "panling.use")
object Item {
    @CommandBody(permission = "panling.admin")
    val main = mainCommand {
        createHelper()
    }

    @CommandBody(permission = "panling.admin")
    val debug = subCommand {
        execute<Player> { sender, _, _ ->
            val allIds = ItemAPI.getAllItemIds()
            sender.sendMessage("§a=== 所有物品ID ===")
            if (allIds.isEmpty()) {
                sender.sendMessage("§c没有找到任何物品ID!")
            } else {
                allIds.forEach { id ->
                    sender.sendMessage("§7- §f$id")
                }
            }
        }
    }

    @CommandBody(permission = "panling.admin")
    val givetool = subCommand {
        execute<Player> { sender, _, _ ->
            val item = buildItem(XMaterial.REDSTONE) {
                name = "§b朝露物品工具"
                lore += "§7左键打开缓存箱子"
                lore += "§7右键打开物品管理面板"
            }
            item.regions = Regions.COMMON_ITEMS
            sender.inventory.addItem(item)
        }
    }

    @CommandBody(permission = "panling.admin")
    val update = subCommand {
        execute<Player> { sender, _, _ ->
            sender.sendMessage("§e正在更新全服玩家物品...")

            val onlineCount = Bukkit.getOnlinePlayers().size
            sender.sendMessage("§7当前在线玩家: §f${onlineCount}人")

            // 静默更新所有玩家
            updateAllPlayersItems(silent = true)

            sender.sendMessage("§a已发送更新指令到所有在线玩家!")
            sender.sendMessage("§7更新将在后台静默进行，玩家无感知")
        }
    }

    @CommandBody(permission = "panling.admin")
    val updateitem = subCommand {
        dynamic(comment = "物品ID") {
            execute<Player> { sender, _, argument ->
                val itemId = argument

                // 检查物品是否存在
                if (!ItemAPI.hasItemId(itemId)) {
                    sender.sendMessage("§c物品ID '$itemId' 不存在!")
                    return@execute
                }

                sender.sendMessage("§e正在更新物品: §f$itemId")

                // 统计拥有该物品的玩家
                val playersWithItem = Bukkit.getOnlinePlayers().filter { player ->
                    player.inventory.contents.any { item ->
                        item?.itemMeta?.persistentDataContainer?.get(
                            ltd.mc233.itemManager.system.Recipe.itemId,
                            org.bukkit.persistence.PersistentDataType.STRING
                        ) == itemId
                    }
                }

                sender.sendMessage("§7找到 §f${playersWithItem.size}名 §7玩家拥有此物品")

                // 通知更新指定物品
                notifyItemUpdated(itemId)

                sender.sendMessage("§a已向拥有物品的玩家发送更新!")
            }
        }
    }

    @CommandBody(permission = "panling.admin")
    val refresh = subCommand {
        execute<Player> { sender, _, _ ->
            sender.sendMessage("§e正在强制刷新所有物品数据...")

            val allItemIds = ItemAPI.getAllItemIds()
            sender.sendMessage("§7共找到 §f${allItemIds.size}个 §7不同的物品ID")

            // 通知更新所有物品
            ltd.mc233.itemManager.system.notifyItemsUpdated(allItemIds)

            sender.sendMessage("§a已发送刷新指令!")
            sender.sendMessage("§7所有玩家的自定义物品将被更新")
        }
    }
}