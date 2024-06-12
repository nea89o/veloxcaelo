package moe.nea.caelo.optifine

import moe.nea.caelo.config.CConfig
import moe.nea.caelo.event.ResourceReloadEvent
import moe.nea.caelo.util.MC
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.LogManager
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.regex.Pattern

object OptifineRegexCache {
	val cache: MutableMap<String, Pattern> = mutableMapOf()
	val illegalRegexes = mutableSetOf<String>()
	val logger = LogManager.getLogger()
	val neverRegex = Pattern.compile("$.")

	@SubscribeEvent
	fun onResourcePackReload(resourceReload: ResourceReloadEvent) {
		cache.clear()
	}

	private fun compilePattern(regex: String): Pattern {
		return try {
			Pattern.compile(regex)
		} catch (ex: Exception) {
			logger.error("Invalid regex $regex in optifine resource pack", ex)
			illegalRegexes.add(regex)
			neverRegex
		}
	}

	fun matchesRegex(str: String, regex: String, cir: CallbackInfoReturnable<Boolean>) {
		if (!CConfig.config.optiCache.regexCache) return
		val pattern = cache.computeIfAbsent(regex, ::compilePattern)
		cir.returnValue = pattern.matcher(str).matches()
	}

	fun printStats() {
		MC.display("Regex Stats:")
		MC.display("- Cache Size: §a${cache.size}")
		if (illegalRegexes.isNotEmpty()) {
			MC.display("- Illegal Regexes:")
			for (illegalRegex in illegalRegexes) {
				MC.display("  - §c${illegalRegex}")
			}
		}
	}


}