import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.DuplicatesStrategy

fun propAsBoolean(name: String): Boolean = (findProperty(name) as String?)?.toBoolean() ?: false

fun propAsInt(name: String, defaultValue: Int): Int =
    (findProperty(name) as String?)?.toIntOrNull() ?: defaultValue

plugins {
    java
    id("me.champeau.jmh") version "0.7.3"
}

group = "express.mvp.myra.codec"
version = "0.2.0"

repositories {
    mavenCentral()
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":codegen"))
    implementation(project(":runtime"))
    implementation("express.mvp:roray-ffm:0.2.0")
    implementation(libs.findLibrary("kryo").get())
    implementation(libs.findLibrary("avro").get())
    implementation(libs.findLibrary("sbe").get())
    implementation(libs.findLibrary("flatbuffers").get())

    jmh(project(":codegen"))
    jmh(project(":runtime"))
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    jmh("express.mvp:roray-ffm:0.2.0")
    jmh(libs.findLibrary("kryo").get())
    jmh(libs.findLibrary("avro").get())
    jmh(libs.findLibrary("sbe").get())
    jmh(libs.findLibrary("flatbuffers").get())

    val jacksonVersion = "2.18.2"
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    jmh("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
}

sourceSets {
    val generatedDir = "src/generated/java"
    named("main") {
        java.srcDir(generatedDir)
    }
    named("jmh") {
        java.srcDir(generatedDir)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

val quickModeEnabled = propAsBoolean("jmh.quick")

jmh {
    duplicateClassesStrategy = DuplicatesStrategy.WARN
    jvmArgsAppend = listOf(
        "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED"
    )
    if (quickModeEnabled) {
        warmupIterations = propAsInt("jmh.quickWarmups", 1)
        iterations = propAsInt("jmh.quickIterations", 1)
        fork = propAsInt("jmh.quickForks", 1)
    }
}
