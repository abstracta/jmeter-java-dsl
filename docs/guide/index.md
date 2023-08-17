# User guide

Here we share some tips and examples on how to use the DSL to tackle common use cases.

Provided examples use [JUnit 5](https://junit.org/junit5/) and [AssertJ](https://joel-costigliola.github.io/assertj/assertj-core-quick-start.html), but you can use other test & assertion libraries.

Explore the DSL in your preferred IDE to discover all available features, and consider reviewing [existing tests](/jmeter-java-dsl/src/test/java/us/abstracta/jmeter/javadsl) for additional examples.

The DSL currently supports most common used cases, keeping it simple and avoiding investing development effort in features that might not be needed. If you identify any particular scenario (or JMeter feature) that you need and is not currently supported, or easy to use, **please let us know by [creating an issue](https://github.com/abstracta/jmeter-java-dsl/issues)** and we will try to implement it as soon as possible. Usually porting JMeter features is quite fast.

::: tip
If you like this project, **please give it a star ⭐ in [GitHub](https://github.com/abstracta/jmeter-java-dsl)!** This helps the project be more visible, gain relevance, and encourages us to invest more effort in new features.
:::

For an intro to JMeter concepts and components, you can check [JMeter official documentation](http://jmeter.apache.org/usermanual/get-started.html).

<!-- @include: setup.md -->
<!-- @include: simple-test-plan.md -->
<!-- @include: recorder/index.md -->
<!-- @include: jmx2dsl.md -->
<!-- @include: scale/index.md -->
<!-- @include: autostop.md -->
<!-- @include: thread-groups/index.md -->
<!-- @include: debugging/index.md -->
<!-- @include: reporting/index.md -->
<!-- @include: response-processing/index.md -->
<!-- @include: request-generation/index.md -->
<!-- @include: variables-and-properties/index.md -->
<!-- @include: test-resources.md -->
<!-- @include: protocols/index.md -->
<!-- @include: wrapper.md -->
<!-- @include: jmx.md -->
