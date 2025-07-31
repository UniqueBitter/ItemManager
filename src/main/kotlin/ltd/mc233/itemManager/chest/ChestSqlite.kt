package ltd.mc233.itemManager.chest


import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.module.database.ColumnTypeSQLite
import taboolib.module.database.Table
import taboolib.module.database.getHost


object ChestSqlite {
    val host = newFile(getDataFolder(), "chest.db").getHost()
    val dataSource by lazy { host.createDataSource() }
    val table = Table("chest", host) {
        add("player") {
            type(ColumnTypeSQLite.TEXT)
        }
        add("slot"){
            type(ColumnTypeSQLite.INTEGER)
        }
        add("item") {
            type(ColumnTypeSQLite.TEXT)
        }
    }

    init {
        // 创建表
        table.createTable(dataSource)
    }




}
