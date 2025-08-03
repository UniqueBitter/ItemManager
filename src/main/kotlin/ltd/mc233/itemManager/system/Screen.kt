package ltd.mc233.itemManager.system

import ltd.mc233.itemManager.api.ItemAPI
import ltd.mc233.itemManager.api.Regions
import ltd.mc233.itemManager.system.Util.regions
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.StorableChest
import taboolib.platform.util.buildItem
import java.util.*

object Screen {
    // 添加临时存储
    val tempItems = HashMap<UUID, MutableMap<Int, ItemStack?>>()
    val currentRegion = HashMap<UUID, Regions>()

    fun open(player: Player, region: Regions, pageNum: Int = 0) {
        val uuid = player.uniqueId

        // 初始化临时存储
        if (tempItems[uuid] == null) {
            tempItems[uuid] = HashMap()
            currentRegion[uuid] = region
            // 加载数据到临时存储
            val savedItems = ItemAPI.getItems(region)
            savedItems.forEach { (slot, item) ->
                tempItems[uuid]!![slot] = item
            }
        }

        player.openMenu<StorableChest>("${region.displayName} §8- §7第${pageNum + 1}页") {
            // 设置6行界面 (54槽位)
            rows(6)

            // 每页显示45个物品槽位
            val itemsPerPage = 45
            val startSlot = pageNum * itemsPerPage

            // 从临时存储获取物品数量和数据
            val totalItems = tempItems[uuid]!!.values.count { it != null }
            val totalPages = (totalItems + itemsPerPage - 1) / itemsPerPage

            // 从临时存储显示当前页的物品
            for (i in 0 until itemsPerPage) {
                val actualSlot = startSlot + i
                val item = tempItems[uuid]!![actualSlot]
                if (item != null) {
                    set(i, item) { }
                }
            }

            // 设置底部功能栏
            setupBottomBar(region, player, pageNum, totalPages, totalItems)

            // 界面关闭时自动保存
            onClose { event ->
                saveToRegion(event, region, player, pageNum)
            }

            // 添加全局点击事件处理
            onClick { event ->
                val clickedSlot = event.rawSlot
                // 只有功能按钮区域 (45-53) 禁止操作，其他都允许
                event.isCancelled = clickedSlot in 45..53
                if (clickedSlot < 45 && event.clickEvent().isRightClick) {
                    event.isCancelled = true
                    val actualSlot = pageNum * 45 + clickedSlot // 计算原始槽位
                    Recipe.attSet(player, event.currentItem, actualSlot)
                }
            }
        }
    }

    private fun StorableChest.setupBottomBar(region: Regions, player: Player, currentPage: Int, totalPages: Int, totalItems: Int) {
        val bottomRow = 45 // 第6行起始位置
        // 上一页按钮
        if (currentPage > 0) {
            set(bottomRow, buildItem(ItemCache.lastPage) {
                name = "§a上一页"
                lore += "§7当前页: §f${currentPage + 1}/$totalPages"
                lore += "§7点击查看上一页"
            }) {
                // 保存当前页到临时存储
                saveCurrentPageToTemp(player, currentPage)
                open(player, region, currentPage - 1)
                isCancelled = true
            }
        } else {
            set(bottomRow, ItemCache.bar) { }
        }

        // 返回主菜单按钮
        set(bottomRow + 1, buildItem(XMaterial.BARRIER) {
            name = "§c返回"
            lore += "§7点击返回物品管理主界面"
        }) {
            isCancelled = true
            ItemManager.open(player)
        }

        // 区域信息展示
        set(bottomRow + 4, buildItem(XMaterial.BOOK) {
            name = "§e${region.displayName}"
            lore += "§7区域ID: §f${region.id}"
            lore += "§7总物品数量: §a$totalItems"
            lore += ""
            lore += "§7§o将物品放入上方区域即可自动保存"
        }) {
            isCancelled = true
        }

        // 清空当前区域按钮
        set(bottomRow + 7, buildItem(XMaterial.LAVA_BUCKET) {
            name = "§c清空区域"
            lore += "§7点击清空当前区域所有物品"
            lore += "§c§l注意: 此操作不可撤销!"
            lore += "§c将清空所有 $totalItems 个物品"
        }) {
            // 清空数据库中的记录 - 创建空背包保存
            //val emptyInv = org.bukkit.Bukkit.createInventory(null, 45)
            //ItemAPI.saveItems(region, emptyInv)
            //player.sendMessage("§a已清空 ${region.displayName} §a区域的所有 $totalItems 个物品!")
            // 重新打开第一页来刷新界面
            player.sendMessage("§c没写！")
        }

        // 下一页按钮
        if (currentPage < totalPages - 1) {
            set(bottomRow + 8, buildItem(ItemCache.nextPage) {
                name = "§a下一页"
                lore += "§7当前页: §f${currentPage + 1}/$totalPages"
                lore += "§7点击查看下一页"
            }) {
                // 保存当前页到临时存储
                saveCurrentPageToTemp(player, currentPage)
                open(player, region, currentPage + 1)
            }
        } else {
            set(bottomRow + 8, ItemCache.bar) { }
        }

        // 填充其他空槽位
        for (i in listOf(bottomRow + 2, bottomRow + 3, bottomRow + 5, bottomRow + 6)) {
            set(i, ItemCache.bar) { }
        }
    }

    // 保存当前页到临时存储
    private fun saveCurrentPageToTemp(player: Player, currentPage: Int) {
        val uuid = player.uniqueId
        val inv = player.openInventory.topInventory
        val itemsPerPage = 45
        val startSlot = currentPage * itemsPerPage

        for (i in 0 until 45) {
            val item = inv.getItem(i)
            val actualSlot = startSlot + i
            if (item != null && item.type != org.bukkit.Material.AIR) {
                val region = currentRegion[uuid] ?: Regions.COMMON_ITEMS
                item.regions = region
                tempItems[uuid]!![actualSlot] = item
            } else {
                tempItems[uuid]!![actualSlot] = null
            }
        }
    }

    private fun saveToRegion(event: InventoryCloseEvent, region: Regions, player: Player, currentPage: Int) {
        try {
            val uuid = player.uniqueId
            val itemsPerPage = 45
            val startSlot = currentPage * itemsPerPage

            // 更新当前页到临时存储
            for (i in 0 until 45) {
                val item = event.inventory.getItem(i)
                val actualSlot = startSlot + i
                if (item != null && item.type != org.bukkit.Material.AIR) {
                    item.regions = region
                    tempItems[uuid]!![actualSlot] = item
                } else {
                    tempItems[uuid]!![actualSlot] = null
                }
            }

            // 从临时存储保存到数据库
            val maxSlot = tempItems[uuid]!!.keys.filter { tempItems[uuid]!![it] != null }.maxOrNull() ?: 0
            val tempSize = ((maxSlot / 9) + 1) * 9
            val tempInventory = org.bukkit.Bukkit.createInventory(null, minOf(tempSize, 54))

            tempItems[uuid]!!.forEach { (slot, item) ->
                if (slot < tempInventory.size && item != null) {
                    tempInventory.setItem(slot, item)
                }
            }

            // 保存到数据库
            ItemAPI.saveItems(region, tempInventory)
            cleanup(player) // 清理数据

        } catch (e: Exception) {
            player.sendMessage("§c保存物品时发生错误: ${e.message}")
            e.printStackTrace()
        }
    }

    // 更新临时存储中的物品
    fun updateTempItem(player: Player, slot: Int, item: ItemStack) {
        val uuid = player.uniqueId
        if (tempItems[uuid] == null) {
            tempItems[uuid] = HashMap()
        }
        tempItems[uuid]!![slot] = item
    }

    // 清理玩家数据
    fun cleanup(player: Player) {
        val uuid = player.uniqueId
        tempItems.remove(uuid)
        currentRegion.remove(uuid)
    }
}