package moe.nea.caelo.event

import net.minecraftforge.fml.common.eventhandler.Event

data class NeaTickEvent(
    val tickCounter: Int,
) : Event()