package moe.nea.caelo

import moe.nea.caelo.event.NeaTickEvent
import moe.nea.caelo.event.ResourceReloadEvent
import moe.nea.caelo.init.MixinPlugin
import moe.nea.caelo.optifine.OptifineCustomItemCache
import moe.nea.caelo.util.InterModUtil
import moe.nea.caelo.util.MC
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.IReloadableResourceManager
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@Mod(modid = "veloxcaelo", useMetadata = true, version = "1.0.2")
class Caelo {
	@SubscribeEvent
	fun onTick(tick: ClientTickEvent) {
		if (tick.phase != TickEvent.Phase.END)
			return
		if (Minecraft.getMinecraft().thePlayer == null)
			return
		MinecraftForge.EVENT_BUS.post(NeaTickEvent(tickCount++))
		if (toOpen != null) {
			Minecraft.getMinecraft().displayGuiScreen(toOpen)
			toOpen = null
		}
	}

	companion object {
		var toOpen: GuiScreen? = null
	}

	var tickCount = 0

	@Mod.EventHandler
	fun onInit(event: FMLInitializationEvent) {
		if (InterModUtil.isOptifineLoaded) {
			MinecraftForge.EVENT_BUS.register(OptifineCustomItemCache)
		}
		MinecraftForge.EVENT_BUS.register(this)
		(Minecraft.getMinecraft().resourceManager as IReloadableResourceManager).registerReloadListener {
			MinecraftForge.EVENT_BUS.post(ResourceReloadEvent())
		}
		ClientCommandHandler.instance.registerCommand(CaeloCommand)
		CaeloCommand.subcommand("mixins") { args ->
			MC.display("Injected mixins:")
			MixinPlugin.loadedMixinClasses.forEach {
				MC.display(" - $it")
			}
		}
	}
}