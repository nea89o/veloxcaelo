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
import java.lang.ref.WeakReference

object OptifineCustomItemCache {

	init {
		CaeloCommand.subcommand("opticache") { args ->
			val cache = cacheSizeHistory.lastOrNull() ?: CacheStats()
			MC.display("OptiCache stats:")
			MC.display("- History: §3${cacheSizeHistory.size}")
			MC.display("- Misses: §c${cache.cacheMisses}")
			MC.display("- Hits: §a${cache.cacheHits}")
			MC.display("- Insertions: §b${cache.insertions}")
			MC.display("- Evictions: §b${cache.removals}")
			MC.display("- Cache Size: §b${cache.size}")
		}
	}

	class CacheKey(val itemStack: WeakReference<ItemStack>, val type: Int) {
		override fun equals(other: Any?): Boolean {
			if (other !is CacheKey) return false
			return itemStack.get() === other.itemStack.get() && type == other.type
		}

		override fun hashCode(): Int {
			return System.identityHashCode(itemStack.get()) * 31 + type
		}

		fun isPresent(): Boolean {
			return itemStack.get() != null
		}
	}

	data class CacheStats(
		var cacheHits: Int = 0,
		var cacheMisses: Int = 0,
		var insertions: Int = 0,
		var size: Int = 0,
		var removals: Int = 0,
	)

	private var map = mutableMapOf<CacheKey, CustomItemProperties?>()
	private val cacheSizeHistory = Histogram<CacheStats>(1000)
	private var cacheStats = CacheStats()

	@SubscribeEvent
	fun onTick(event: NeaTickEvent) {
		var removeCount = 0
		val nextMap = mutableMapOf<CacheKey, CustomItemProperties?>()
		for (entry in map) {
			if (entry.key.isPresent()) {
				nextMap[entry.key] = entry.value
			} else {
				removeCount++
			}
		}
		map = nextMap
		cacheStats.size = map.size
		cacheStats.removals = removeCount
		cacheSizeHistory.append(cacheStats)
		cacheStats = CacheStats()

	}

	@JvmStatic
	fun retrieveCacheHit(
		itemStack: ItemStack,
		type: Int,
		cir: CallbackInfoReturnable<CustomItemProperties?>
	) {
		if (!CConfig.config.optiCache.citCache)
			return
		val key = CacheKey(WeakReference(itemStack), type)
		if (!map.containsKey(key)) {
			cacheStats.cacheMisses++
			return
		}
		cacheStats.cacheHits++
		cir.returnValue = map[key]
	}

	@JvmStatic
	fun storeCustomItemProperties(itemStack: ItemStack, type: Int, cip: CustomItemProperties) {
		map[CacheKey(WeakReference(itemStack), type)] = cip
		cacheStats.insertions++
	}

	@JvmStatic
	fun storeNoCustomItemProperties(itemStack: ItemStack, type: Int) {
		map[CacheKey(WeakReference(itemStack), type)] = null
		cacheStats.insertions++
	}
}