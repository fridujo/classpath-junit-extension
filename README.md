# Classpath modification extension for JUnit5
[![Build Status](https://travis-ci.com/fridujo/classpath-junit-extension.svg?branch=master)](https://travis-ci.com/fridujo/classpath-junit-extension)
[![Coverage Status](https://codecov.io/gh/fridujo/classpath-junit-extension/branch/master/graph/badge.svg)](https://codecov.io/gh/fridujo/classpath-junit-extension/)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.fridujo/classpath-junit-extension.svg)](https://search.maven.org/#search|ga|1|a:"classpath-junit-extension")
[![JitPack](https://jitpack.io/v/fridujo/classpath-junit-extension.svg)](https://jitpack.io/#fridujo/classpath-junit-extension)
[![License](https://img.shields.io/github/license/fridujo/classpath-junit-extension.svg)](https://opensource.org/licenses/Apache-2.0)

Extension to run tests with classpath customizations.

The main goal of this project is to allow to write tests (most probably **integration** ones) against various classpaths
without the need to create complex configurations astride _build tool_ and code.

For example, testing a library behavior without an optional dependency.

## Testing optional dependency

```java
@Test
void junit_extension_can_be_loaded() throws ClassNotFoundException {
    assertThat(Class.forName("org.junit.jupiter.api.extension.Extension")).isExactlyInstanceOf(Class.class);
}

@Test
@ModifiedClasspath(excludeDependencies = "junit-jupiter-api")
void junit_extension_cannot_be_loaded() {
    assertThatExceptionOfType(ClassNotFoundException.class)
        .isThrownBy(() -> Class.forName("org.junit.jupiter.api.extension.Extension"));
}
```

## Testing retro-compatibility

```java
@CompatibilityTestWithClasspath(dependencies = {
    "spring-rabbit:[1.7.7.RELEASE, 2.0.14.RELEASE, 2.2.4.RELEASE]"
})
void amqp_basic_get() {
    String messageBody = "Hello world!";
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AmqpConfiguration.class)) {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "test.key1", messageBody);

        Message message = rabbitTemplate.receive(QUEUE_NAME);

        assertThat(message).isNotNull();
        assertThat(message.getBody()).isEqualTo(messageBody.getBytes());
    }
}
```

## Alternatives
Use the `maven-invoker-plugin` with **pom.xml** template (see an example [here](https://github.com/fridujo/rabbitmq-mock/blob/78cd20380ea46089193dfbf5e29efd55798343ee/pom.xml#L163)).

## Roadmap
Currently this extension uses a _workaround_ to get things done, but it is waiting for [JUnit5 #201](https://github.com/junit-team/junit5/issues/201) to get a cleaner approach at this.

Next things to do:
* Replace dependencies by other ones (different versions or implementations)
* Support other **Build Tools** (Gradle, SBT, Ivy, etc.)
* Make the annotation
  * available at class level
  * work in `@Nested` tests
  * work in conjunction with **injection** / **test-templates** (may require **the classloader extension**)
  * repeatable, so that the same test can be expected to work against various classpath (different version of a library per se)

## Contribute
Any contribution is greatly appreciated.

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#github.com/fridujo/classpath-junit-extension.git)

## Getting Started

### Maven
Add the following dependency to your **pom.xml**
```xml
<dependency>
    <groupId>com.github.fridujo</groupId>
    <artifactId>classpath-junit-extension</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

### Gradle
Add the following dependency to your **build.gradle**
```groovy
repositories {
	mavenCentral()
}

// ...

dependencies {
	// ...
	testCompile('com.github.fridujo:classpath-junit-extension:1.0.0')
	// ...
}
```

### Building from Source

You need [JDK-8+](http://jdk.java.net/8/) (at least) to build this extension. The project can be built with Maven using the following command.
```
mvn clean package
```

### Installing in the Local Maven Repository

The project can be installed in a local Maven Repository for usage in other projects via the following command.
```
mvn clean install
```

### Using the latest SNAPSHOT

The master of the project pushes SNAPSHOTs in Sonatype's repo.

To use the latest master build add Sonatype OSS snapshot repository, for Maven:
```
<repositories>
    ...
    <repository>
        <id>sonatype-oss-spanshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    </repository>
</repositories>
```

For Gradle:
```groovy
repositories {
    // ...
    maven {
        url "https://oss.sonatype.org/content/repositories/snapshots"
    }
}
