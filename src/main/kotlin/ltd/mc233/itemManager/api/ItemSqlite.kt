package ltd.mc233.itemManager.api

import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.module.database.ColumnTypeSQLite
import taboolib.module.database.Table
import taboolib.module.database.getHost

object ItemSqlite {
    val host = newFile(getDataFolder(), "ItemManager.db").getHost()
    val dataSource by lazy { host.createDataSource() }
    val table = Table("ItemManager", host) {
        add("region") {
            type(ColumnTypeSQLite.INTEGER)
        }
        add("item") {
            type(ColumnTypeSQLite.TEXT)
        }
        add("slot") {
            type(ColumnTypeSQLite.INTEGER)
        }
    }

    init {
        // 创建表
        table.createTable(dataSource)
    }


}