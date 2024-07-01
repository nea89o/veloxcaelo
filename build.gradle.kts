import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import java.io.ByteArrayOutputStream

plugins {
	kotlin("jvm") version "1.9.22"
	`maven-publish`
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("xyz.wagyourtail.unimined") version "1.2.0-SNAPSHOT"
}


fun cmd(vararg args: String): String? {
	val output = ByteArrayOutputStream()
	val r = exec {
		this.commandLine(args.toList())
		this.isIgnoreExitValue = true
		this.standardOutput = output
		this.errorOutput = ByteArrayOutputStream()
	}
	return if (r.exitValue == 0) output.toByteArray().decodeToString().trim()
	else null
}

val tag = cmd("git", "describe", "--tags", "HEAD")
val hash = cmd("git", "rev-parse", "--short", "HEAD")!!
val isSnapshot = tag == null || hash in tag
group = "moe.nea"
version = (if (isSnapshot) hash else tag!!)

repositories {
	maven("https://jitpack.io")
	maven("https://repo.polyfrost.cc/releases")
	maven("https://maven.notenoughupdates.org/releases/")
	mavenCentral()
	maven("https://nea.moe/redir-repo") {
		metadataSources { artifact() }
		content {
			includeGroup("optifine")
		}
	}
}

val optifineConfig by configurations.creating {
}
configurations.compileOnly {
	extendsFrom(optifineConfig)
}

unimined.minecraft {
	version("1.8.9")
	mappings {
		searge()
		mcp("stable", "22-1.8.9")
	}
	minecraftForge {
		loader("11.15.1.2318-1.8.9")
		mixinConfig("veloxcaelo.mixins.json")
	}
	mods {
		remap(optifineConfig) {
			namespace("official")
		}
	}
	runs {
		this.config("client") {
			this.args.addAll(
				listOf(
					"--mods", optifineConfig.resolve().joinToString(",") { it.toRelativeString(this.workingDir) },
					"--tweakClass", "org.spongepowered.asm.launch.MixinTweaker",
					"--tweakClass", "io.github.notenoughupdates.moulconfig.tweaker.DevelopmentResourceTweaker",
				)
			)
		}
	}
}


val shadowModImpl by configurations.creating {
	configurations.named("modImplementation").get().extendsFrom(this)
}
val shadowImpl by configurations.creating {
	configurations.implementation.get().extendsFrom(this)
}

dependencies {
	testImplementation("org.jetbrains.kotlin:kotlin-test")
	shadowImpl("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
		isTransitive = false
	}
	shadowModImpl("org.notenoughupdates.moulconfig:legacy:3.0.0-beta.7") {
		isTransitive = false
	}
	optifineConfig("optifine:optifine:1.8.9")
	compileOnly("org.jetbrains:annotations:24.1.0")
}

tasks.test {
	useJUnitPlatform()
}

sourceSets.main {
	output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
	kotlin.destinationDirectory.set(java.destinationDirectory)
}
tasks.processResources {
	filesMatching("*.mixins.json") {
		this.autoDiscoverMixins(sourceSets.main.get())
	}
}

tasks.compileJava {
	dependsOn(tasks.processResources)
}
java {
	targetCompatibility = JavaVersion.VERSION_1_8
	sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType(JavaCompile::class) {
	this.options.encoding = "UTF-8"
}
tasks.withType(KotlinCompile::class) {
	this.compilerOptions {
		this.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
	}
}

tasks.withType(Jar::class) {
	destinationDirectory.set(project.layout.buildDirectory.dir("badjars"))
	archiveBaseName.set("VeloxCaelo")
	manifest.attributes.run {
		this["FMLCorePluginContainsFMLMod"] = "true"
		this["ForceLoadAsMod"] = "true"

		// If you don't want mixins, remove these lines
		this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
		this["MixinConfigs"] = "veloxcaelo.mixins.json"
	}
}
tasks.shadowJar {
	archiveClassifier.set("dep-dev")
	configurations = listOf(shadowImpl, shadowModImpl)
	relocate("io.github.notenoughupdates.moulconfig", "moe.nea.velox.moulconfig")
	mergeServiceFiles()
}
tasks.named<RemapJarTask>("remapJar") {
	this.inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
	dependsOn((tasks.shadowJar))
}