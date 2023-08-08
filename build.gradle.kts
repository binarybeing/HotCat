plugins {
    id("org.jetbrains.intellij") version "1.0"
}
dependencies {
    //gson
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.apache.commons:commons-jexl3:3.1")
    implementation("org.reflections:reflections:0.9.10")

}
group = "io.github.binarybeing.hotcat"
version = "1.3.5.181212.2"

repositories {
    mavenCentral()
}


// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2018.1")
    type.set("IU") //// Target IDE Platform
    plugins.set(listOf("org.jetbrains.plugins.terminal"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "8"
        targetCompatibility = "8"
    }

    patchPluginXml {
        sinceBuild.set("181.0")
        untilBuild.set("212.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

}

