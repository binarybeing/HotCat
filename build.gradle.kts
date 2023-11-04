plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.0"
}
dependencies {
    //gson
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.apache.commons:commons-jexl3:3.1")
    implementation("org.reflections:reflections:0.9.10")
    implementation("com.github.javaparser:javaparser-core:3.24.2")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.24.2")
    implementation("ognl:ognl:3.3.4")
    implementation("io.grpc:grpc-netty-shaded:1.48.0")
    implementation("io.grpc:grpc-protobuf:1.48.0")
    implementation("io.grpc:grpc-stub:1.48.0")
    implementation("io.github.binarybeing.hotcat:porto:1.0-SNAPSHOT")
}
group = "io.github.binarybeing.hotcat"
version = "1.3.6.193231.1"

repositories {
    mavenCentral()
    mavenLocal()
}



// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2022.1")
    //version.set("2021.3")
    type.set("IU") //// Target IDE Platform

    plugins.set(listOf("org.jetbrains.plugins.terminal","java", "org.intellij.plugins.markdown"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("193.0")
        untilBuild.set("231.*")
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
