plugins {
    `java-library`
    alias(libs.plugins.spotless)
    alias(libs.plugins.spotbugs)
    checkstyle
}

group = "express.mvp.myra.codec"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
    api("com.fasterxml.jackson.core:jackson-databind:2.18.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
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
    toolVersion = libs.versions.checkstyle.get()
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

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.12.1")
        }
    }
}
