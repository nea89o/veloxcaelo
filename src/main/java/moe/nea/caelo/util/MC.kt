package moe.nea.caelo.util

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.ChatComponentText

object MC {
    fun display(text: String) {
        player?.addChatMessage(ChatComponentText("§b[Velox] §f$text"))
    }

    val player: EntityPlayerSP? get() = Minecraft.getMinecraft().thePlayer
}