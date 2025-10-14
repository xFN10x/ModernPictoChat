plugins {
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
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
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "server.App"
}
