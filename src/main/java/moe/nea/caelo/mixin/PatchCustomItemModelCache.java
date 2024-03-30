package moe.nea.caelo.mixin;

import moe.nea.caelo.optifine.OptifineCustomItemCache;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.optifine.CustomItemProperties;
import net.optifine.CustomItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Pseudo
@Mixin(value = CustomItems.class, remap = false)
public class PatchCustomItemModelCache {

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "getCustomItemProperties", at = @At("HEAD"), cancellable = true)
    private static void overrideCustomItemProperties(
            ItemStack itemStack, int type,
            CallbackInfoReturnable<CustomItemProperties> cir) {
        OptifineCustomItemCache.retrieveCacheHit(itemStack, type, cir);
    }

    @Inject(method = "getCustomItemProperties", at = @At(value = "RETURN", ordinal = 2),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void storeCustomItemProperties(
            ItemStack itemStack, int type, CallbackInfoReturnable<CustomItemProperties> cir,
            Item item, int itemId, CustomItemProperties[] cips, int i, CustomItemProperties cip) {
        OptifineCustomItemCache.storeCustomItemProperties(itemStack, type, cip);
    }

    @Inject(method = "getCustomItemProperties", at = @At(value = "RETURN", ordinal = 3))
    private static void storeCustomItemProperties(
            ItemStack itemStack, int type, CallbackInfoReturnable<CustomItemProperties> cir) {
        OptifineCustomItemCache.storeNoCustomItemProperties(itemStack, type);
    }


}