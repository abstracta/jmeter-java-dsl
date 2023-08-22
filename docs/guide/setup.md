## Setup

To use the DSL just include it in your project:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl</artifactId>
  <version>1.19</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
// this is required due to JMeter open issue: https://bz.apache.org/bugzilla/show_bug.cgi?id=64465
@CacheableRule
class JmeterRule implements ComponentMetadataRule {
    void execute(ComponentMetadataContext context) {
        context.details.allVariants {
            withDependencies {
                removeAll { it.group == "org.apache.jmeter" && it.name == "bom" }
            }
        }
    }
}

dependencies {
    ...
    testImplementation 'us.abstracta.jmeter:jmeter-java-dsl:1.19'
    components {
        withModule("org.apache.jmeter:ApacheJMeter_core", JmeterRule)
        withModule("org.apache.jmeter:ApacheJMeter_java", JmeterRule)
        withModule("org.apache.jmeter:ApacheJMeter", JmeterRule)
        withModule("org.apache.jmeter:ApacheJMeter_http", JmeterRule)
        withModule("org.apache.jmeter:ApacheJMeter_functions", JmeterRule)
        withModule("org.apache.jmeter:ApacheJMeter_components", JmeterRule)
        withModule("org.apache.jmeter:ApacheJMeter_config", JmeterRule)
        withModule("org.apache.jmeter:jorphan", JmeterRule)
    }
}
```
:::
::::

::: tip
[Here](https://github.com/abstracta/jmeter-java-dsl-sample) is a sample project in case you want to start one from scratch.
:::