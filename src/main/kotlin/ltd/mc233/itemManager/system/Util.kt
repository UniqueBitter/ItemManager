package ltd.mc233.itemManager.system

import ltd.mc233.itemManager.api.ItemAPI
import ltd.mc233.itemManager.api.Regions
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

object Util {

    /**
     * 将ItemStack物品序列化为Base64编码的字符串
     *
     * @return 序列化后的Base64字符串，可用于物品数据的存储和传输
     */
// 物品 → Base64字符串
    fun ItemStack.itemToBase64(): String {
        // 使用字节输出流和Bukkit对象输出流将物品栈序列化为字节数组
        return ByteArrayOutputStream().use { output ->
            BukkitObjectOutputStream(output).use {
                it.writeObject(this)
                // 将字节数组编码为Base64字符串并返回
                Base64.getEncoder().encodeToString(output.toByteArray())
            }
        }
    }

    /**
     * 将Base64编码的字符串反序列化为ItemStack对象
     *
     * @return 反序列化后的ItemStack对象
     **/
// Base64字符串 → 物品
    fun String.base64ToItem(): ItemStack {
        // 将Base64字符串解码为字节数组，然后通过Bukkit对象输入流反序列化为ItemStack
        return Base64.getDecoder().decode(this).let { bytes ->
            ByteArrayInputStream(bytes).use { input ->
                BukkitObjectInputStream(input).use { it.readObject() as ItemStack }
            }
        }
    }

    var ItemStack.regions: Regions?
        get() = this.itemMeta?.persistentDataContainer?.get(ItemManager.region, PersistentDataType.INTEGER)
            ?.let { Regions.getById(it) }
        set(value) {
            value?.let {
                val meta = this.itemMeta ?: return
                meta.persistentDataContainer.set(ItemManager.region, PersistentDataType.INTEGER, it.id)
                this.itemMeta = meta
            }
        }

    // 给予物品
    fun Player.giveItem(itemId: String, amount: Int = 1): Boolean {
        return ItemAPI.giveItem(this, itemId, amount)
    }

    // 检测是否有物品
    fun Player.hasItem(itemId: String): Boolean {
        return ItemAPI.hasItem(this, itemId)
    }

    // 检测是否有足够数量的物品
    fun Player.hasItem(itemId: String, amount: Int): Boolean {
        return ItemAPI.hasItem(this, itemId, amount)
    }

    // 获取物品数量
    fun Player.getItemCount(itemId: String): Int {
        return ItemAPI.getItemCount(this, itemId)
    }

    // 扣除物品
    fun Player.takeItem(itemId: String, amount: Int = 1): Boolean {
        return ItemAPI.takeItem(this, itemId, amount)
    }

    // 获取物品模板
    fun Player.getCustomItem(itemId: String): ItemStack? {
        return ItemAPI.getItemById(itemId)
    }

    // 获取物品在背包中的位置
    fun Player.getItemSlots(itemId: String): List<Int> {
        return ItemAPI.getItemSlots(this, itemId)
    }

    // 批量给予物品 - 多种调用方式
    fun Player.giveItems(vararg items: Pair<String, Int>): Int {
        return ItemAPI.giveItems(this, items.toMap())
    }

    fun Player.giveItems(items: Map<String, Int>): Int {
        return ItemAPI.giveItems(this, items)
    }

    // 简化的批量给予 - 直接传入参数
    fun Player.giveItems(itemId1: String, amount1: Int): Int {
        return this.giveItems(itemId1 to amount1)
    }

    fun Player.giveItems(itemId1: String, amount1: Int, itemId2: String, amount2: Int): Int {
        return this.giveItems(itemId1 to amount1, itemId2 to amount2)
    }

    fun Player.giveItems(itemId1: String, amount1: Int, itemId2: String, amount2: Int, itemId3: String, amount3: Int): Int {
        return this.giveItems(itemId1 to amount1, itemId2 to amount2, itemId3 to amount3)
    }

    // 批量扣除物品
    fun Player.takeItems(vararg items: Pair<String, Int>): Boolean {
        return ItemAPI.takeItems(this, items.toMap())
    }

    fun Player.takeItems(items: Map<String, Int>): Boolean {
        return ItemAPI.takeItems(this, items)
    }

    // 简化的批量扣除
    fun Player.takeItems(itemId1: String, amount1: Int): Boolean {
        return this.takeItems(itemId1 to amount1)
    }

    fun Player.takeItems(itemId1: String, amount1: Int, itemId2: String, amount2: Int): Boolean {
        return this.takeItems(itemId1 to amount1, itemId2 to amount2)
    }

    // 检测是否有所有指定的物品
    fun Player.hasAllItems(vararg items: Pair<String, Int>): Boolean {
        return items.all { (itemId, amount) ->
            this.hasItem(itemId, amount)
        }
    }

    // 简化的检测
    fun Player.hasAllItems(itemId1: String, amount1: Int): Boolean {
        return this.hasAllItems(itemId1 to amount1)
    }

    fun Player.hasAllItems(itemId1: String, amount1: Int, itemId2: String, amount2: Int): Boolean {
        return this.hasAllItems(itemId1 to amount1, itemId2 to amount2)
    }

// 获取和设置物品的自定义ID
    var ItemStack.itemId: String
        get() = this.itemMeta?.persistentDataContainer?.get(Recipe.itemId, PersistentDataType.STRING) ?: "unknown_item"
        set(value) {
            val meta = this.itemMeta ?: return
            meta.persistentDataContainer.set(Recipe.itemId, PersistentDataType.STRING, value)
            this.itemMeta = meta
        }
}