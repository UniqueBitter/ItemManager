package ltd.mc233.itemManager.api

enum class Regions(val id: Int, val displayName: String) {
    /** 任务物品 */
    QUEST_ITEMS(0, "§c任务物品"),

    /** 食物 */
    FOOD(1, "§a食物"),

    /** 元素 */
    ELEMENTS(2, "§b元素"),

    /** 装备原核 */
    EQUIPMENT_CORE(3, "§6装备原核"),

    /** 怪物掉落物 */
    MONSTER_DROPS(4, "§f怪物掉落物"),

    /** 采集物 */
    COLLECTION_ITEMS(5, "§a采集物"),

    /** 未解封物品 */
    UNSEALED_ITEMS(6, "§c未解封物品"),

    /** 常用物品 */
    COMMON_ITEMS(9, "§b常用");

    companion object {
        fun getById(id: Int): Regions? = entries.find { it.id == id }
    }
}