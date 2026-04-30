plugins {
    `java-library`
    alias(libs.plugins.spotless)
    alias(libs.plugins.spotbugs)
    checkstyle
    id("com.vanniktech.maven.publish")
}

group = "express.mvp.myra"
version = "0.2.1"

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
    api("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    compileOnly(libs.spotbugs.annotations)
    testCompileOnly(libs.spotbugs.annotations)
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
    exclude("**/module-info.java")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// SpotBugs configuration
spotbugs {
    toolVersion.set("4.9.8")
    ignoreFailures.set(false)
    showStackTraces.set(true)
    showProgress.set(true)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    enabled = true
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

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = group.toString(),
        artifactId = "codec-schema-core",
        version = version.toString(),
    )

    pom {
        name.set("myra-codec-schema-core")
        description.set("Core schema definitions for Myra codec framework.")
        inceptionYear.set("2025")
        url.set("https://github.com/mvp-express/myra-codec")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("mvp-express")
                name.set("MVP Express Team")
                email.set("hi@mvp.express")
            }
        }

        scm {
            url.set("https://github.com/mvp-express/myra-codec")
            connection.set("scm:git:git://github.com/mvp-express/myra-codec.git")
            developerConnection.set("scm:git:ssh://git@github.com/mvp-express/myra-codec.git")
        }
    }
}
