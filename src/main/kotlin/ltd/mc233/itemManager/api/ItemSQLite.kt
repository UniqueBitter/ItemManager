package ltd.mc233.itemManager.api

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import taboolib.common.io.newFile
import taboolib.common.io.newFolder
import taboolib.common.platform.function.getDataFolder
import taboolib.module.database.ColumnTypeSQLite
import taboolib.module.database.Table
import taboolib.module.database.getHost
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

object ItemSQLite {
    init {
        //创建dewitem物品文件夹
        newFolder(getDataFolder(), "DewItem", true)
        //init块是类或对象被创建时自动执行的代码块，相当于构造函数的一部分。
        //自动初始化：object被首次访问时，数据库环境自动准备好
        //正确顺序：文件夹 → 数据库文件 → 数据库连接 → 数据库表
        //一次性设置：初始化代码只在object创建时执行一次
    }

    val file = getDataFolder().listFiles()?.find {
        it.name == "DewItem"  // 找到DewItem文件夹,赋予变量file
    }!!

    val host = newFile(file, "items.db").getHost()  // 创建items.db数据库文件
    val name = "items"


    val tablePack = Table("${name}_data", host) {
        add("item") {
            type(ColumnTypeSQLite.TEXT, 512)    // 物品Base64字符串
        }
        add("region") {
            type(ColumnTypeSQLite.INTEGER)      // 类别id
        }
        add("slot") {
            type(ColumnTypeSQLite.INTEGER)      // 槽位ID
        }
    }

    val dataSource = host.createDataSource()
    // 建立与SQLite数据库文件items.db的连接,
    // 创建一个连接池来管理数据库连接

    init {
        //必须等所有属性初始化完毕后才能创建表
        tablePack.createTable(dataSource)
    }


    fun getItems() {
        tablePack.select(dataSource) {
            rows("item", "region", "slot")
        }.map {
            ItemAPI.itemDatas.add(ItemData(getString("item").base64ToItem(), getInt("region"), getInt("slot")))
        }
    }

    fun saveItems(itemDatas: HashSet<ItemData>) {
        tablePack.delete(dataSource) {
            this.elements
        }
        itemDatas.forEach {
            tablePack.insert(dataSource, "item", "region", "slot") {
                value(it.item.itemToBase64(), it.region, it.slot)
            }
        }
    }


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


}