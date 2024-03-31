package moe.nea.caelo.config

import io.github.notenoughupdates.moulconfig.managed.ManagedConfig
import java.io.File

object CConfig {
    val managed = ManagedConfig.create(
        File("veloxcaelo/config.json").absoluteFile,
        ConfigData::class.java
    )
    val config get() = managed.instance
}