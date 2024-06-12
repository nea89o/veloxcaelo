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
import java.lang.ref.ReferenceQueue
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
			OptifineRegexCache.printStats()
		}
	}

	val referenceQueue = ReferenceQueue<ItemStack>()

	class CacheKeyReference(val cacheKey: CacheKey, itemStack: ItemStack) :
		WeakReference<ItemStack>(itemStack, referenceQueue)

	class CacheKey(itemStack: ItemStack, val type: Int) {
		val hashCode = System.identityHashCode(itemStack) * 31 + type
		val ref = CacheKeyReference(this, itemStack)

		override fun equals(other: Any?): Boolean {
			if (other === this) return true
			if (other !is CacheKey) return false
			return ref.get() === other.ref.get() && type == other.type
		}

		override fun hashCode(): Int {
			return hashCode
		}

		fun isPresent(): Boolean {
			return ref.get() != null
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
		while (true) {
			val ref = referenceQueue.poll() as CacheKeyReference? ?: break
			removeCount++
			map.remove(ref.cacheKey)
		}
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
		cacheStats.insertions++
	}

	@JvmStatic
	fun storeNoCustomItemProperties(itemStack: ItemStack, type: Int) {
		map[CacheKey(itemStack, type)] = null
		cacheStats.insertions++
	}
}