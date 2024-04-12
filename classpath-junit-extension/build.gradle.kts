plugins {
    `maven-publish`
}

group = "com.github.fridujo"
version = "1.0.1-SNAPSHOT"

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

dependencies {
    compileOnly(libs.org.apache.maven.resolver.maven.resolver.api)
    compileOnly(libs.org.apache.maven.resolver.maven.resolver.spi)
    compileOnly(libs.org.apache.maven.resolver.maven.resolver.impl)
    compileOnly(libs.org.junit.jupiter.junit.jupiter.api)

    api(libs.ch.qos.logback.logback.classic)
    api(libs.eu.maveniverse.maven.mima.context)
    api(libs.com.fasterxml.jackson.dataformat.jackson.dataformat.xml)

    runtimeOnly(libs.eu.maveniverse.maven.mima.runtime.standalone.static)

    testImplementation(libs.org.junit.jupiter.junit.jupiter)
    testImplementation(libs.org.assertj.assertj.core)
    testImplementation(libs.org.mockito.mockito.core)
    testImplementation(libs.com.google.guava.guava)
}
