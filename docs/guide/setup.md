## Setup

To use the DSL just include it in your project:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl</artifactId>
  <version>2.1</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation("us.abstracta.jmeter:jmeter-java-dsl:2.1") {
    exclude("org.apache.jmeter", "bom")
}
```
:::
::::

::: tip
[Here](https://github.com/abstracta/jmeter-java-dsl-sample) is a sample project in case you want to start one from scratch.
:::