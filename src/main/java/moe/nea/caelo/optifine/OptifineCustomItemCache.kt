package moe.nea.caelo.optifine

import moe.nea.caelo.CaeloCommand
import moe.nea.caelo.config.CConfig
import moe.nea.caelo.event.NeaTickEvent
import moe.nea.caelo.util.Histogram
import moe.nea.caelo.util.MC
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.optifine.CustomItemProperties
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

object OptifineCustomItemCache {

    init {
        CaeloCommand.subcommand("opticache") { args ->
            val cache = cacheSizeHistory.lastOrNull() ?: CacheStats()
            MC.display("OptiCache stats:")
            MC.display("- History: §3${cacheSizeHistory.size}")
            MC.display("- Misses: §c${cache.cacheMisses}")
            MC.display("- Hits: §a${cache.cacheHits}")
            MC.display("- Entries: §b${cache.uniquePropertyBearingStacks}")
        }
    }

    class CacheKey(val itemStack: ItemStack, val type: Int) {
        override fun equals(other: Any?): Boolean {
            if (other !is CacheKey) return false
            return itemStack === other.itemStack && type == other.type
        }

        override fun hashCode(): Int {
            return System.identityHashCode(itemStack) + type * 31
        }
    }

    data class CacheStats(
        var cacheHits: Int = 0,
        var cacheMisses: Int = 0,
        var uniquePropertyBearingStacks: Int = 0,
    )

    private val map = mutableMapOf<CacheKey, CustomItemProperties?>()
    private val cacheSizeHistory = Histogram<CacheStats>(1000)
    private var cacheStats = CacheStats()

    @SubscribeEvent
    fun onTick(event: NeaTickEvent) {
        cacheSizeHistory.append(cacheStats)
        cacheStats = CacheStats()
        map.clear()
    }

    @JvmStatic
    fun retrieveCacheHit(
        itemStack: ItemStack,
        type: Int,
        cir: CallbackInfoReturnable<CustomItemProperties?>
    ) {
        if (!CConfig.config.optiCache.citCache)
            return
        val key = CacheKey(itemStack, type)
        if (!map.containsKey(key)) {
            cacheStats.cacheMisses++
            return
        }
        cacheStats.cacheHits++
        cir.returnValue = map[key]
    }

    @JvmStatic
    fun storeCustomItemProperties(itemStack: ItemStack, type: Int, cip: CustomItemProperties) {
        map[CacheKey(itemStack, type)] = cip
        cacheStats.uniquePropertyBearingStacks++
    }

    @JvmStatic
    fun storeNoCustomItemProperties(itemStack: ItemStack, type: Int) {
        map[CacheKey(itemStack, type)] = null
    }
}