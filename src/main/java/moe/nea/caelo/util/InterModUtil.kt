package moe.nea.caelo.util

import net.minecraftforge.fml.client.FMLClientHandler

object InterModUtil {
    val isOptifineLoaded get() = FMLClientHandler.instance().hasOptifine()

}
