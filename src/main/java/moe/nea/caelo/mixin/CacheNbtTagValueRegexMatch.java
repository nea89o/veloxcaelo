package moe.nea.caelo.mixin;

import moe.nea.caelo.optifine.OptifineRegexCache;
import net.optifine.config.NbtTagValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtTagValue.class)
public class CacheNbtTagValueRegexMatch {
	@Inject(method = "matchesRegex", at = @At("HEAD"), cancellable = true)
	private void onMatchesRegex(String str, String regex, CallbackInfoReturnable<Boolean> cir) {
		OptifineRegexCache.INSTANCE.matchesRegex(str, regex, cir);
	}
}
