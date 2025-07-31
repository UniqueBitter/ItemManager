package ltd.mc233.itemManager.system

import ltd.mc233.itemManager.api.Regions
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
        get() = this.itemMeta?.persistentDataContainer?.get(ItemManager.region, PersistentDataType.INTEGER)?.let { Regions.getById(it) }
        set(value) {
            value?.let {
                val meta = this.itemMeta ?: return
                meta.persistentDataContainer.set(ItemManager.region, PersistentDataType.INTEGER, it.id)
                this.itemMeta = meta
            }
        }




}