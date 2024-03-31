package moe.nea.caelo.config

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.Category

class ConfigData : Config() {

    @Category(name = "OptiCache", desc = "Optifine speed improvements")
    @JvmField
    @Expose
    val optiCache = OptiCache()
    override fun saveNow() {
        CConfig.managed.saveToFile()
    }

    override fun getTitle(): String {
        return "§bVelox Caelo§7 by §anea89"
    }
}