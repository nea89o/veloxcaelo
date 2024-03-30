import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    `maven-publish`
    id("xyz.wagyourtail.unimined") version "1.2.0-SNAPSHOT"
}

group = "moe.nea"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://jitpack.io")
    maven("https://repo.polyfrost.cc/releases")
    mavenCentral()
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
    runs {
        this.config("client") {
            this.args.addAll(listOf("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker"))
            this.env.put(
                "LD_LIBRARY_PATH",
                ":/nix/store/agp6lqznayysqvqkx4k1ggr8n1rsyi8c-gcc-13.2.0-lib/lib:/nix/store/ldi0rb00gmbdg6915lhch3k3b3ib460z-libXcursor-1.2.2/lib:/nix/store/8xbbv82pabjcbj30vrna4gcz4g9q97z4-libXrandr-1.5.4/lib:/nix/store/smrb2g0addhgahkfjjl3k8rfd30gdc29-libXxf86vm-1.1.5/lib:/nix/store/lpqy1z1h8li6h3cp9ax6vifl71dks1ff-libglvnd-1.7.0/lib"
            )
        }
    }
}


val downloadOptifine by tasks.creating {
    val outputFile = layout.buildDirectory.file("download/optifine.jar")
    outputs.file(outputFile)
    doLast {
        outputFile.get().asFile.parentFile.mkdirs()
        uri("https://optifine.net/download?f=preview_OptiFine_1.8.9_HD_U_M6_pre2.jar")
            .toURL()
            .openStream().use { input ->
                outputFile.get().asFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
    }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    compileOnly(project.files(downloadOptifine))
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
        this.filter(
            mapOf("sourceRoots" to sourceSets.main.get().allSource.srcDirs
                .filter { it.exists() }.joinToString(":") { it.absolutePath }),
            MixinFilterReader::class.java
        )//TODO: keep old existing class names in mixins array
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