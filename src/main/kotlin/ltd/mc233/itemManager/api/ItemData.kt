package ltd.mc233.itemManager.api

import net.minecraft.world.item.ItemStack

data class ItemData(
    val item: ItemStack,    // 物品本身
    val region: Int,        // 属于哪个分类
    val slot: Int          // 在界面中的位置
)
