plugins {
    application
    id("com.gradleup.shadow") version "9.2.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.guava)

    // https://mvnrepository.com/artifact/jakarta.websocket/jakarta.websocket-api
    implementation("jakarta.websocket:jakarta.websocket-api:2.2.0")

    // https://mvnrepository.com/artifact/org.eclipse.jetty.websocket/websocket-jakarta-server
    implementation("org.eclipse.jetty.websocket:websocket-jakarta-server:11.0.26")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.13.2")

    // https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5
    implementation("org.apache.httpcomponents.client5:httpclient5:5.5.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "fn10.server.App"
}

tasks.test {
    failOnNoDiscoveredTests = false
}

tasks.shadowJar {
  archiveBaseName = "server"
  archiveVersion = ""
  archiveClassifier = ""
  destinationDirectory = layout.buildDirectory.dir("builtJars")
}