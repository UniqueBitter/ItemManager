package ltd.mc233.itemManager.system


import ltd.mc233.itemManager.api.ItemAPI
import ltd.mc233.itemManager.api.Regions
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.StorableChest
import taboolib.platform.util.buildItem

object Screen {
    fun open(player: Player, region: Regions, pageNum: Int = 0) {
        player.openMenu<StorableChest>("${region.displayName} §8- §7第${pageNum + 1}页") {
            // 设置6行界面 (54槽位)
            rows(6)

            // 每页显示45个物品槽位
            val itemsPerPage = 45
            val startSlot = pageNum * itemsPerPage

            // 加载该区域已保存的物品
            val savedItems = ItemAPI.getItems(region)
            val totalItems = savedItems.size
            val totalPages = (totalItems + itemsPerPage - 1) / itemsPerPage

            // 显示当前页的物品
            savedItems.entries.drop(startSlot).take(itemsPerPage).forEachIndexed { index, (originalSlot, item) ->
                set(index, item) {
                }
            }

            // 设置底部功能栏
            setupBottomBar(region, player, pageNum, totalPages, totalItems)

            // 界面关闭时自动保存
            onClose { event ->
                saveToRegion(event, region, player, pageNum)
            }
        }
    }

    private fun StorableChest.setupBottomBar(region: Regions, player: Player, currentPage: Int, totalPages: Int, totalItems: Int) {
        val bottomRow = 45 // 第6行起始位置
        // 上一页按钮
        if (currentPage > 0) {
            set(bottomRow, buildItem(XMaterial.ARROW) {
                name = "§a上一页"
                lore += "§7当前页: §f${currentPage + 1}/$totalPages"
                lore += "§7点击查看上一页"
            }) {
                open(player, region, currentPage - 1)
            }
        } else {
            set(bottomRow, ItemCache.bar) {
                isCancelled = true
            }
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
            isCancelled = true
            // 清空数据库中的记录 - 创建空背包保存
            //val emptyInv = org.bukkit.Bukkit.createInventory(null, 45)
            //ItemAPI.saveItems(region, emptyInv)
            //player.sendMessage("§a已清空 ${region.displayName} §a区域的所有 $totalItems 个物品!")
            // 重新打开第一页来刷新界面
            player.sendMessage("§c没写！")
            open(player, region, 0)
        }

        // 下一页按钮
        if (currentPage < totalPages - 1) {
            set(bottomRow + 8, buildItem(XMaterial.ARROW) {
                name = "§a下一页"
                lore += "§7当前页: §f${currentPage + 1}/$totalPages"
                lore += "§7点击查看下一页"
            }) {
                open(player, region, currentPage + 1)
            }
        } else {
            set(bottomRow + 8,ItemCache.bar) {
                isCancelled = true
            }
        }

        // 填充其他空槽位
        for (i in listOf(bottomRow + 2, bottomRow + 3, bottomRow + 5, bottomRow + 6)) {
            set(i, ItemCache.bar) {
                isCancelled = true
            }
        }
    }

    private fun saveToRegion(event: InventoryCloseEvent, region: Regions, player: Player, currentPage: Int) {
        try {
            val itemsPerPage = 45
            val startSlot = currentPage * itemsPerPage

            // 获取现有的所有物品
            val existingItems = ItemAPI.getItems(region).toMutableMap()

            // 清除当前页对应的物品槽位
            val keysToRemove = existingItems.keys.filter { it >= startSlot && it < startSlot + itemsPerPage }
            keysToRemove.forEach { existingItems.remove(it) }

            // 添加界面中的新物品
            for (i in 0 until 45) {
                val item = event.inventory.getItem(i)
                if (item != null && item.type != org.bukkit.Material.AIR) {
                    existingItems[startSlot + i] = item
                }
            }

            // 创建临时背包保存所有物品
            val maxSlot = existingItems.keys.maxOrNull() ?: 0
            val tempSize = ((maxSlot / 9) + 1) * 9 // 向上取整到9的倍数
            val tempInventory = org.bukkit.Bukkit.createInventory(null, minOf(tempSize, 54))

            existingItems.forEach { (slot, item) ->
                if (slot < tempInventory.size) {
                    tempInventory.setItem(slot, item)
                }
            }

            // 保存到数据库
            ItemAPI.saveItems(region, tempInventory)
            player.sendMessage("§a第${currentPage + 1}页物品已保存到 ${region.displayName} §a区域!")

        } catch (e: Exception) {
            player.sendMessage("§c保存物品时发生错误: ${e.message}")
            e.printStackTrace()
        }
    }
}