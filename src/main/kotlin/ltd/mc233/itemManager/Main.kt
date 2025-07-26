package ltd.mc233.itemManager

import ltd.mc233.itemManager.api.ItemAPI
import ltd.mc233.itemManager.api.ItemSQLite
import org.bukkit.Bukkit
import taboolib.common.platform.Plugin


object Main : Plugin() {
    val console = Bukkit.getConsoleSender()
    val onlinePlayer = Bukkit.getOnlinePlayers()
    val plugin = this
    val world = Bukkit.getWorld("world")

    override fun onEnable() {
        console.sendMessage(
            """             
        §9 ██████████████████ 朝露物品管理器加载完成 █████████████████████
        """
        )
        ItemAPI.loadItems()

    }
}