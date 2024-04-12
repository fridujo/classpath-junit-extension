group = "com.github.fridujo"
version = "1.0.1-SNAPSHOT"
description = "classpath-junit-extension"

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    apply {
        plugin("base")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
        (options as CoreJavadocOptions).addStringOption("Xdoclint:all,-missing", "-quiet")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("java-library")
    }
}
