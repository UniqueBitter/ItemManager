package ltd.mc233.itemManager.system

import ltd.mc233.itemManager.api.Regions
import ltd.mc233.itemManager.system.Util.regions
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.StorableChest
import taboolib.platform.util.buildItem
import java.util.*

object Recipe {
    val itemId = NamespacedKey("pl_att", "id")
    // 修改存储结构，保存物品和槽位信息
    val itemSet = HashMap<UUID, Pair<ItemStack, Int?>>()
    val nameSet = HashMap<UUID, Pair<ItemStack, Int?>>()
    val loreSet = HashMap<UUID, Pair<ItemStack, Int?>>()

    fun attSet(player: Player, item: ItemStack?, originalSlot: Int? = null) {
        if (item == null) return
        val uuid = player.uniqueId
        val region = item.regions ?: Regions.COMMON_ITEMS
        player.openMenu<StorableChest>("§b物品管理") {
            map(
                "####E####",
                "1 2 3 4 5",
                "#########",
                "6 7 8 9 a",
                "U########",
            )
            set('#', ItemCache.bar.clone()) {
                isCancelled = true
            }
            set('E', item.clone()){
                isCancelled = true
            }
            set('U', ItemCache.lastPage.clone()) {
                isCancelled = true
                // 保存修改后的物品到临时存储
                if (originalSlot != null) {
                    Screen.updateTempItem(player, originalSlot, item)
                    isCancelled = true
                }
                isCancelled = true
                Screen.open(player, region, 0)
            }
            set('1', buildItem(XMaterial.NAME_TAG) {
                name = "§auid设置"
                lore.add("§7当前uid:${item.persistentDataContainer.get(itemId, PersistentDataType.STRING)}")
            }) {
                isCancelled = true
                player.sendMessage("§a请输入要设置的uid")
                itemSet[uuid] = Pair(item, originalSlot)
                player.closeInventory()
            }
            set('2', buildItem(XMaterial.DIAMOND_CHESTPLATE) {
                name = "§6丢弃保护"
                if (item.persistentDataContainer.get(DropProtect.drop, PersistentDataType.BOOLEAN) == true) {
                    lore.add("§f当前丢弃保护:§a开")
                } else lore.add("§f当前丢弃保护:§c关")
                hideAll()
            }) {
                isCancelled = true
                val meta = item.itemMeta
                if (meta.persistentDataContainer.get(DropProtect.drop, PersistentDataType.BOOLEAN) != true) {
                    meta.persistentDataContainer.set(DropProtect.drop, PersistentDataType.BOOLEAN, true)
                } else meta.persistentDataContainer.set(DropProtect.drop, PersistentDataType.BOOLEAN, false)
                item.itemMeta = meta
                // 更新临时存储
                if (originalSlot != null) {
                    Screen.updateTempItem(player, originalSlot, item)
                }
                attSet(player, item, originalSlot)
            }
            set('3', buildItem(XMaterial.PAPER) {
                name = "§a名称"
                lore.add("§7点击设置物品名称")
            }) {
                isCancelled = true
                player.sendMessage("§a请输入要设置的物品名称")
                nameSet[uuid] = Pair(item, originalSlot)
                player.closeInventory()
            }
            set('4', buildItem(XMaterial.NAME_TAG) {
                name = "§7描述"
                lore.add("§7左键点击添加物品描述")
                lore.add("§7右键点击删除一行物品描述")
            }) {
                isCancelled = true
                if (clickEvent().isLeftClick) {
                    player.sendMessage("§a请输入要添加的物品描述")
                    loreSet[uuid] = Pair(item, originalSlot)
                    player.closeInventory()
                } else if (clickEvent().isRightClick) {
                    val lore = item.lore()
                    lore?.removeLastOrNull()
                    item.lore(lore)
                    // 更新临时存储
                    if (originalSlot != null) {
                        Screen.updateTempItem(player, originalSlot, item)
                    }
                    attSet(player, item, originalSlot)
                }
            }
        }
    }

    @SubscribeEvent
    fun chat(event: PlayerChatEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val message = event.message
        when (uuid) {
            in itemSet -> {
                val (item, slot) = itemSet[uuid]!!
                val meta = item.itemMeta
                meta.persistentDataContainer.set(itemId, PersistentDataType.STRING, message)
                item.itemMeta = meta
                itemSet.remove(uuid)

                // 更新临时存储
                if (slot != null) {
                    Screen.updateTempItem(player, slot, item)
                }

                attSet(player, item, slot)
                player.sendMessage("§a物品uid已设置为${message}")
                event.isCancelled = true
            }

            in nameSet -> {
                val (item, slot) = nameSet[uuid]!!
                val meta = item.itemMeta
                meta.displayName(Component.translatable(message.colored()))
                item.itemMeta = meta
                nameSet.remove(uuid)

                // 更新临时存储
                if (slot != null) {
                    Screen.updateTempItem(player, slot, item)
                }

                attSet(player, item, slot)
                player.sendMessage("§a已成功设置物品名称")
                event.isCancelled = true
            }

            in loreSet -> {
                val (item, slot) = loreSet[uuid]!!
                val lore = item.lore() ?: mutableListOf()
                lore.add(Component.translatable(message.colored()))
                item.lore(lore)
                loreSet.remove(uuid)

                // 更新临时存储
                if (slot != null) {
                    Screen.updateTempItem(player, slot, item)
                }

                attSet(player, item, slot)
                player.sendMessage("§a已成功添加物品lore")
                event.isCancelled = true
            }
        }
    }
}