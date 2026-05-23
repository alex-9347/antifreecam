import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.1"
}

group = "de.mcmdev"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "codemc"
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    runServer {
        jvmArgs("-XX:+AllowEnhancedClassRedefinition")
        minecraftVersion("1.21.10")
    }
}

paperPluginYaml {
    main = "de.mcmdev.antifreecam.AntiFreecamPlugin"
    apiVersion = "1.21"
}