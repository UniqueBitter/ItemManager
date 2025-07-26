package ltd.mc233.itemManager

import ltd.mc233.itemManager.api.ItemAPI
import ltd.mc233.itemManager.api.ItemData
import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.StorableChest
import taboolib.platform.util.buildItem
import java.util.*

object Screen {

    // 存储每个玩家的当前页码
    private val playerPages = HashMap<UUID, Int>()

    // 存储每个玩家当前查看的region 类型
    private val playerRegions = HashMap<UUID, Int>()

    fun option(player: Player, region: Int) {
        val uuid = player.uniqueId
        playerPages[uuid] = 1
        playerRegions[uuid] = region
        openItemScreen(player, region, 1)
    }

    fun openItemScreen(player: Player, region: Int, page: Int) {
        player.openMenu<StorableChest>("§b 物品管理") {
            map(
                "#########",
                "#       #",
                "#       #",
                "#       #",
                "#       #",
                "###<U>###",
            )
            set('#', ItemCache.bar.clone()) { isCancelled = true }

            val itemSlots = (0..3).flatMap { row ->
                (10 + row * 9)..(16 + row * 9)
            }

            val regionItems = ItemAPI.itemDatas.filter { it.region == region }
                .sortedBy { it.slot }

            val startIndex = (page - 1) * 35
            val pageItems = regionItems.drop(startIndex).take(35)

            // 显示已有物品
            pageItems.forEachIndexed { index, itemData ->
                if (index < itemSlots.size) {
                    set(itemSlots[index], itemData.item.clone()) {
                        // 只有在物品槽位范围内才处理给物品逻辑
                        val clickedSlot = clickEvent().rawSlot
                        if (clickedSlot in itemSlots) {
                            when {
                                clickEvent().isRightClick -> {
                                    ItemAPI.itemDatas.remove(itemData)
                                    ItemAPI.saveItems()
                                    openItemScreen(player, region, page)
                                }
                                clickEvent().isShiftClick && clickEvent().isLeftClick -> {
                                    val item = itemData.item.clone()
                                    item.amount = item.maxStackSize
                                    player.inventory.addItem(item)
                                }
                                clickEvent().isLeftClick -> {
                                    val item = itemData.item.clone()
                                    item.amount = 1
                                    player.inventory.addItem(item)
                                }
                            }
                        }
                        isCancelled = true
                    }
                }
            }

            onClose {
                try {
                    val inventory = it.inventory
                    val tempItems = mutableListOf<ItemData>()

                    // 将非当前页的物品添加到临时列表
                    val pageSlotRange = startIndex until (startIndex + 35)
                    tempItems.addAll(ItemAPI.itemDatas.filter {
                            itemData -> !(itemData.region == region && itemData.slot in pageSlotRange)
                    })

                    // 添加当前页中有物品的槽位
                    itemSlots.forEachIndexed { index, slot ->
                        val item = inventory.getItem(slot)
                        if (item != null && item.type != Material.AIR) {
                            val saveItem = item.clone()
                            val globalSlot = startIndex + index
                            tempItems.add(ItemData(saveItem, region, globalSlot))
                        }
                    }
                    // 清空原数据并添加所有新的数据
                    ItemAPI.itemDatas.clear()
                    ItemAPI.itemDatas.addAll(tempItems)
                    ItemAPI.saveItems()
                } catch (e: Exception) {
                    player.sendMessage("§c保存物品时发生错误: ${e.message}")
                    e.printStackTrace()
                }
            }

            set('<', ItemCache.lastPage.clone()) {
                if (page > 1) openItemScreen(player, region, page - 1)
                isCancelled = true
            }

            set('>', ItemCache.nextPage.clone()) {
                val hasMoreItems = startIndex + pageItems.size < regionItems.size
                if (hasMoreItems) openItemScreen(player, region, page + 1)
                isCancelled = true
            }

            set('U', buildItem(Material.ELYTRA) {
                name = "§b返回"
                hideAll()
                shiny()
            }) {
                ItemManager.open(player)
                isCancelled = true
            }
        }
    }
}