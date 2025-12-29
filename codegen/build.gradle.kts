import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    application
    alias(libs.plugins.spotless)
    alias(libs.plugins.spotbugs)
    checkstyle
    id("com.gradleup.shadow") version "9.1.0" 
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

application {
    mainClass.set("express.mvp.myra.codec.codegen.MyraCodegenCli")
}

group = "express.mvp.myra.codec"
version = "0.1.0"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

// Spotless configuration for Google Java Format
spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.32.0").aosp().reflowLongStrings()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

val checkstyleDir = file("${rootProject.projectDir}/config/checkstyle")

// Checkstyle configuration - simplified rules for essential code quality
checkstyle {
    toolVersion = libs.findVersion("checkstyle").get().toString()
    configDirectory.set(checkstyleDir)
    configFile = checkstyleDir.resolve("simple_checks.xml")
    isIgnoreFailures = false
    maxWarnings = 0
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// SpotBugs configuration
// NOTE: SpotBugs is disabled because it does not yet support Java 25 class files (version 69).
// Re-enable when SpotBugs adds support for Java 25+.
spotbugs {
    ignoreFailures.set(true)  // Disabled until Java 25 support is added
    showStackTraces.set(true)
    showProgress.set(true)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
}

// Disable SpotBugs tasks entirely for now
tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    enabled = false  // Disabled until Java 25 support is added
    reports {
        create("html") {
            required.set(true)
            outputLocation.set(file("${layout.buildDirectory.get()}/reports/spotbugs/${name}.html"))
        }
        create("xml") {
            required.set(true)
            outputLocation.set(file("${layout.buildDirectory.get()}/reports/spotbugs/${name}.xml"))
        }
    }
}

dependencies {
    implementation(project(":schema-core"))
    implementation("express.mvp:roray-ffm:0.1.0")
    implementation("com.palantir.javapoet:javapoet:0.7.0")
    implementation("info.picocli:picocli:4.7.7")

    testImplementation(project(":runtime"))
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.12.1")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass.set("express.mvp.myra.codec.codegen.MyraCodegenCli")
}

tasks.named("shadowDistZip") { enabled = false }
tasks.named("shadowDistTar") { enabled = false }
tasks.named("startShadowScripts") { enabled = false }

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "express.mvp.myra.codec.codegen.MyraCodegenCli"
    }
    archiveClassifier.set("all")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "express.mvp.myra.codec.codegen.MyraCodegenCli"
    }
}