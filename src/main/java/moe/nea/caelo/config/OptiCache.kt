package moe.nea.caelo.config

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class OptiCache {
    @ConfigEditorBoolean
    @ConfigOption(name = "Enable CIT cache", desc = "Cache CIT property lookups")
    @Expose
    @JvmField
    var citCache = true
}
