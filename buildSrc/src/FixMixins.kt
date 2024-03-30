import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.apache.tools.ant.filters.BaseParamFilterReader
import java.io.File
import java.io.Reader
import java.io.StringReader

class MixinFilterReader(reader: Reader) : BaseParamFilterReader() {
    lateinit var sourceRoots: String
    val betterReader: StringReader by lazy {
        StringReader(run {
            val json = Gson().fromJson(reader.readText(), JsonObject::class.java)
            val mixinPackage = (json["package"] as JsonPrimitive).asString
            val allMixins = JsonArray()
            sourceRoots
                .split(":")
                .map { File(it) }
                .forEach { base ->
                    base.walk()
                        .filter { it.isFile }
                        .forEach {
                            val relativeString = it.toRelativeString(base).replace("\\", "/")
                            if (relativeString.startsWith(mixinPackage.replace(".", "/") + "/")
                                && relativeString.endsWith(".java")
                                && it.readText().contains("@Mixin")
                            )
                                allMixins.add(
                                    relativeString.replace("/", ".").dropLast(5).drop(mixinPackage.length + 1)
                                )
                        }
                }
            json.add("mixins", allMixins)
            Gson().toJson(json)
        })

    }

    override fun read(): Int {
        return betterReader.read()
    }
}
