package ltd.mc233.itemManager.system

import ltd.mc233.itemManager.api.ItemAPI
import ltd.mc233.itemManager.api.Regions
import ltd.mc233.itemManager.system.Util.giveItems
import ltd.mc233.itemManager.system.Util.itemId
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

    // 统一初始化方法
    private fun ensurePlayerData(player: Player, region: Regions) {
        val uuid = player.uniqueId
        if (tempItems[uuid] == null || currentRegion[uuid] != region) {
            tempItems[uuid] = HashMap()
            currentRegion[uuid] = region
            // 加载数据到临时存储
            val savedItems = ItemAPI.getItems(region)
            savedItems.forEach { (slot, item) ->
                tempItems[uuid]!![slot] = item
            }
        }
    }

    fun open(player: Player, region: Regions, pageNum: Int = 0) {
        // 统一初始化
        ensurePlayerData(player, region)

        val uuid = player.uniqueId

        player.openMenu<StorableChest>("${region.displayName} §8- §7第${pageNum + 1}页") {
            rows(6)
            val itemsPerPage = 45
            val startSlot = pageNum * itemsPerPage
            val totalItems = tempItems[uuid]!!.values.count { it != null }
            val totalPages = maxOf(1, (totalItems + itemsPerPage - 1) / itemsPerPage)

            // 从临时存储显示当前页的物品
            for (i in 0 until itemsPerPage) {
                val actualSlot = startSlot + i
                val item = tempItems[uuid]!![actualSlot]
                if (item != null) {
                    set(i, item) { }
                }
            }

            setupBottomBar(region, player, pageNum, totalPages, totalItems)

            onClose { event ->
                saveToRegion(event, region, player, pageNum)
            }

            onClick { event ->
                val clickedSlot = event.rawSlot
                event.isCancelled = clickedSlot in 45..53
                if (clickedSlot < 45 && event.clickEvent().isRightClick) {
                    event.isCancelled = true
                    val actualSlot = pageNum * 45 + clickedSlot
                    Recipe.attSet(player, event.currentItem, actualSlot)
                }
                if (clickedSlot < 45 && event.clickEvent().isShiftClick&&event.clickEvent().isLeftClick) {
                    event.isCancelled = true
                    event.currentItem?.itemId?.let { player.giveItems(it,64) }
                }
            }
        }
    }

    private fun StorableChest.setupBottomBar(region: Regions, player: Player, currentPage: Int, totalPages: Int, totalItems: Int) {
        val bottomRow = 45

        // 上一页
        if (currentPage > 0) {
            set(bottomRow, buildItem(ItemCache.lastPage) {
                name = "§a上一页"
                lore += "§7当前页: §f${currentPage + 1}/$totalPages"
            }) {
                saveCurrentPageToTemp(player, currentPage)
                open(player, region, currentPage - 1)
                isCancelled = true
            }
        } else {
            set(bottomRow, ItemCache.bar) { }
        }

        // 返回主菜单
        set(bottomRow + 1, buildItem(XMaterial.BARRIER) {
            name = "§c返回"
            lore += "§7点击返回物品管理主界面"
        }) {
            isCancelled = true
            saveCurrentPageToTemp(player, currentPage)
            saveTempItemsToDatabase(player, region)
            cleanup(player)
            ItemManager.open(player)
        }

        // 区域信息
        set(bottomRow + 4, buildItem(XMaterial.BOOK) {
            name = "§e${region.displayName}"
            lore += "§7区域ID: §f${region.id}"
            lore += "§7总物品数量: §a$totalItems"
            lore += ""
            lore += "§7§o将物品放入上方区域即可自动保存"
        }) {
            isCancelled = true
        }

        // 清空区域
        set(bottomRow + 7, buildItem(XMaterial.LAVA_BUCKET) {
            name = "§c清空区域"
            lore += "§7点击清空当前区域所有物品"
            lore += "§c§l注意: 此操作不可撤销!"
        }) {
            isCancelled = true
            tempItems[player.uniqueId]!!.clear()
            val emptyInv = org.bukkit.Bukkit.createInventory(null, 9)
            ItemAPI.saveItems(region, emptyInv)
            player.sendMessage("§a已清空 ${region.displayName} §a区域的所有物品!")
            open(player, region, 0)
        }

        // 下一页
        if (currentPage < totalPages - 1) {
            set(bottomRow + 8, buildItem(ItemCache.nextPage) {
                name = "§a下一页"
                lore += "§7当前页: §f${currentPage + 1}/$totalPages"
            }) {
                saveCurrentPageToTemp(player, currentPage)
                open(player, region, currentPage + 1)
            }
        } else {
            set(bottomRow + 8, ItemCache.bar) { }
        }

        // 填充空槽位
        for (i in listOf(bottomRow + 2, bottomRow + 3, bottomRow + 5, bottomRow + 6)) {
            set(i, ItemCache.bar) { }
        }
    }

    private fun saveCurrentPageToTemp(player: Player, currentPage: Int) {
        val uuid = player.uniqueId
        val inv = player.openInventory.topInventory
        val startSlot = currentPage * 45

        for (i in 0 until 45) {
            val item = inv.getItem(i)
            val actualSlot = startSlot + i
            if (item != null && item.type != org.bukkit.Material.AIR) {
                item.regions = currentRegion[uuid]!!
                tempItems[uuid]!![actualSlot] = item
            } else {
                tempItems[uuid]!![actualSlot] = null
            }
        }
    }

    private fun saveToRegion(event: InventoryCloseEvent, region: Regions, player: Player, currentPage: Int) {
        val uuid = player.uniqueId

        // 添加空值检查和重新初始化
        if (tempItems[uuid] == null || currentRegion[uuid] != region) {
            ensurePlayerData(player, region)
        }

        val startSlot = currentPage * 45

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
        saveTempItemsToDatabase(player, region)
    }

    private fun saveTempItemsToDatabase(player: Player, region: Regions) {
        val uuid = player.uniqueId
        val validSlots = tempItems[uuid]!!.filterValues { it != null }

        if (validSlots.isEmpty()) {
            val emptyInv = org.bukkit.Bukkit.createInventory(null, 9)
            ItemAPI.saveItems(region, emptyInv)
            return
        }

        val maxSlot = validSlots.keys.maxOrNull() ?: 0
        val requiredSize = ((maxSlot / 9) + 1) * 9
        val tempInventory = org.bukkit.Bukkit.createInventory(null, requiredSize)

        validSlots.forEach { (slot, item) ->
            if (slot < tempInventory.size) {
                tempInventory.setItem(slot, item!!)
            }
        }

        ItemAPI.saveItems(region, tempInventory)
    }

    fun updateTempItem(player: Player, slot: Int, item: ItemStack) {
        val uuid = player.uniqueId
        tempItems[uuid]!![slot] = item
    }

    fun cleanup(player: Player) {
        val uuid = player.uniqueId
        tempItems.remove(uuid)
        currentRegion.remove(uuid)
    }
}