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
    
    // https://mvnrepository.com/artifact/org.glassfish.tyrus/tyrus-server
    implementation("org.glassfish.tyrus:tyrus-server:2.2.0")

    // https://mvnrepository.com/artifact/org.glassfish.tyrus/tyrus-container-grizzly-server
    implementation("org.glassfish.tyrus:tyrus-container-grizzly-server:2.2.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "server.App"
}
