plugins {
    `java-library`
    alias(libs.plugins.spotless)
    alias(libs.plugins.spotbugs)
    checkstyle
}

group = "express.mvp.myra.codec"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("express.mvp:roray-ffm:0.1.0")
    implementation(project(":runtime"))
    compileOnly(libs.spotbugs.annotations)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}