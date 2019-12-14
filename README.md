# JUnit5 Classpath modification extension
[![Build Status](https://travis-ci.com/fridujo/classpath-junit-extension.svg?branch=master)](https://travis-ci.com/fridujo/classpath-junit-extension)
[![Coverage Status](https://codecov.io/gh/fridujo/classpath-junit-extension/branch/master/graph/badge.svg)](https://codecov.io/gh/fridujo/classpath-junit-extension/)

Extension to run tests with classpath customizations

The main goal of this project is to allow to write tests (most probably **integration** ones) against various classpaths
without the need to create complex configurations astride _build tool_ and code.

## Sample

```java
@Test
void junit_extension_can_be_loaded() throws ClassNotFoundException {
    assertThat(Class.forName("org.junit.jupiter.api.extension.Extension")).isExactlyInstanceOf(Class.class);
}

@Test
@ModifiedClasspath(excludeGavs = "junit-jupiter-api")
void junit_extension_cannot_be_loaded() {
    assertThatExceptionOfType(ClassNotFoundException.class)
        .isThrownBy(() -> Class.forName("org.junit.jupiter.api.extension.Extension"));
}
```

# Alternatives
Use the `maven-invoker-plugin` with **pom.xml** template (see an example [here](https://github.com/fridujo/rabbitmq-mock/blob/78cd20380ea46089193dfbf5e29efd55798343ee/pom.xml#L163)).

# Roadmap
Currently this extension uses a _workaround_ to get things done, but it is waiting for [JUnit5 #201](https://github.com/junit-team/junit5/issues/201) to get a cleaner approach at this.

Next things to do:
* Use Maven dependency mechanism to add jars that are not already in the classpath
* Make the annotation
  * available at class level
  * work in `@Nested` tests
  * work in conjunction with **injection** / **test-templates** (may require **the classloader extension**)
  * repeatable, so that the same test can be expected to work against various classpath (different version of a library per se)
