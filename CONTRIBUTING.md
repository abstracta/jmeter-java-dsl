# Contributing

This guide will introduce you to the code, to help you understand better how it works and send pull requests to submit contributions.

Before continuing, if you are not familiar with JMeter, please review [JMeter test plan elements](https://jmeter.apache.org/usermanual/test_plan.html) for a basic understanding of JMeter core concepts.

Let's start looking at each of the main classes, and later on, show a diagram to get an overview of their relations.

## Core classes

[JmeterDsl] is the main entry point of the library and provides factory methods that allow creating test plans and running them. Each factory method receives as parameters the main attributes of a test element, which are required in most cases, and, when it is a natural container of other test elements (i.e.: it's of no use when no included children), the list of children test elements to nest on it (eg: thread groups to put on a test plan).

Test elements are classes that implement [DslTestElement] interface, which set up JMeter test elements to be included in a test plan. They might also provide fluent API methods to set optional test element attributes (eg: HTTP method for HTTP sampler) or add children to them.

To implement a DslTestElement, the library already provides some base classes from where to start. Here are the main ones:

* [BaseSampler]: whenever you need to add support for some new protocol, you will need a sampler. You can extend this class to easily start creating a DSL test element for an existing JMeter sampler. Example: [DslHttpSampler]
* [BaseConfigElement]: as you implement samplers, it is usual to share common configurations between several instances of a sampler type (e.g.: connection settings). Config elements are the JMeter solution for this requirement. BaseConfigElement will provide the basics for creating a DSL test element for them. Example: [HttpHeaders]
* [BaseTestElement](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/testelements/BaseTestElement.java): This contains the very basics of test elements, allowing to abstract the common logic of all test elements (ie: naming them, setting common properties and adding them to a JMeter tree). In general, you shouldn't need to create direct subclasses of this class, but use one of the provided base classes (eg: [BaseConfigElement], [BaseSampler], [TestElementContainer], and several others provided by the DSL). Create a subclass of this class only if none of the existing classes match the criteria for the element. Example: [DslDebugPostProcessor](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/timers/DslUniformRandomTimer.java).
* [TestElementContainer]: This represents a test element that can nest other test elements. Only extend this class if none of the existing DSL classes are good enough as parent classes. Example: [DslTestPlan].

As previously mentioned, DslTestElement instances can be nested, and when implementing one, you should define under what other test elements this element can be nested (eg: thread groups can only be added under test plans). To do that, just implement the appropriate interface required by the associated "parent" element, currently provided interfaces are:

* [TestPlanChild](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslTestPlan.java)
* [ThreadGroupChild](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/threadgroups/BaseThreadGroup.java)
* [SamplerChild](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/samplers/BaseSampler.java)

In general, you wouldn't need to directly implement any of these interfaces, since you can use base classes that already implement them (such as BaseSampler, BaseConfigElement, and other similar ones), or can use one of the provided interfaces for each kind of element (eg: [DslTimer](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/timers/DslTimer.java), [DslPostProcessor](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslPostProcessor.java), etc).

### Code generation

[JMeterDsl] also provides code generation allowing to generate DSL code from JMeter test plans, to ease migration and adoption.

This is implemented by DslTestElement nested companion classes named CodeBuilder which extend [MethodCallBuilder](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/codegeneration/MethodCallBuilder.java) and are in charge of generating the DSL code from a JMeter test element for the given DSL test element class.

Whenever you implement a new DslTestElement, you should implement a nested CodeBuilder class to provide proper conversion from JMeter test plan files (JMX files) to DSL code.

Check [DslTestPlan] for an example code builder, and [MethodCallBuilder] for additional details on how to implement one and consider using [SingleTestElementCallBuilder] or [SingleGuiClassCallBuilder] as base classes which simplify most of the scenarios.

## Test runs

When you want to run a test plan, it needs to run in a JMeter engine. By default, DslTestPlan uses [EmbeddedJmeterEngine](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/engines/EmbeddedJmeterEngine.java), which is the fastest and easiest way to run a test plan, but you might use EmbeddedJmeterEngine as an example and implement your custom engine (for example to run tests in some cloud provider like BlazeMeter).

When a test plan runs, the engine returns an instance of [TestPlanStats](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/TestPlanStats.java), grouping information by test element name (aka label). This allows users to check the expected statistics and verify that everything worked within expected boundaries.

## Class diagram

Here is a simplified class diagram of the main described classes:

![image](https://www.plantuml.com/plantuml/png/dLRDRXit4BxlKmnyKWl9Jn08QMKtZX6Z0PosXo07QZdICk7FWfnrdHfwzt99IYB5NRV8YQlXzyt_ZFZM4RCqADcuRqBtp385ocZJTjOe1B83DftW2AXw-WuArHQjYw9Ms5PYqxIIEGVt2WdjZPFmqW1SNpU7fkbP-utjFT9Oz68l5zg3Tgn5yCXFwEXMea9DWGHmnrptoIB2TX2IZrz_Q8vsSP9x3prw2RyUxpT64puJjF7yAQXvCeA3wypVx_MxGIn6uM6q7ZsjkD7s0-lVF98LUh-4txTViQKreFO2TATxx_IVmWrCYd_GBk5nw7ifqBxP6DKRxT7hDJoT02xQpQFEpDvg_a4uGeqMNivkupEs0-5YBiBbEKba4K-nLVZLQWrNCLzNrUqiuUUh6H_F-KkL-oYP1ez1pHrafHofJ9oZEc8VYP43EsXVZqdvUxtmgcCVtUgzLm4OZhoex0b_SVVgUH5B1JiXyIEZBc9Ud5ScsaFXuyuQDScegvCecywdvXHRsQabuyBXsP1pVoXzr0u4Kvr5njzPC_HPTZE_HWRREhAifMD7CuSLRwugGlLHv2xC5HM-mHqR9DLQQsinvh5F_FomFQx1ZOShfovS9Mm86IZeS9b445f6mkYZy6Q8lu6dZpfbcIV_LRQqZa81CEKWhgN9Z6RiB6dJYmiWTPTc2fDa4pJj1n4-aZ2ZnYMN2T9W-D54vgTXPyRzOYp17kaeIfZ3fDUJzSxBLZkUvefn7h9dKI_A03z_WJRTvAQJnLkrHSwH5qDG9Yk_YjIYtn-PHv_ThEevMvwA-7gpPxOklKZYrzkdf3mCaiG7VC8SD3sloogaPM4ngfxqVZNqQIUNtWH6deFhwtMbyv2tl4nRFFHxLOEuuxDSJeOtMucA_lhq7E5v7zsJ_CiAodNaYY-6x2l9tz8MZorPnTiQlHFxwMWtuUu7OutaXFgV0KVEqSVyloaKhjpkKErNZwcdebOpDOeDpEnoJuWREI9ot_rL_Lp8NtCTaL_wnR_5dHULyrZCg4TdrHki33z7qWDIPpYeSEIWhU7VYp-G0Bz3nj6wiusg8lLRBfvhw28tARtqsZz-SWiXuFEJTlMv4voZ_h8ARzdchRS-A7ay_mK0)

You can check more complete and complex diagrams [here](docs/diagrams).

## Implementing a new DSL test element or feature

Here we will detail the main steps and things to take into consideration when implementing a new test element, or extending an existing one.

Before doing any coding, consider that the project uses Java 8, maven 3.5, and docker as main dependencies, so make sure that you install them beforehand.

Additionally, the project uses checkstyle for enforcing code consistency and conventions. You can get IDEs settings from [here](https://github.com/google/styleguide), which already matches the code style rules. Using IDEs autoformatting with such configurations should automatically fix most styling issues.

1. Evaluate if you want to contribute the code to the community
   * Any contribution will be welcomed by the community, and you can pay a little debt for your usual consumption of community resources :).
     In some cases though, the logic might be too specific for a scenario and/or sensitive to share with the rest, so you might opt to use an internal repository or custom logic for your particular project.
2. Evaluate if a new DSL test element is needed or maybe an existing one should be extended (add a method or logic to it).
   * If you are just planning to add some feature (eg: support for JMeter property not yet supported) to an already supported JMeter test element, then you highly probably just need to add a new method to the associated DslTestElement and modify its buildTestElement method.
   * If the test element you are planning to add is not yet supported by the DSL, then in most cases you will need to implement a new DslTestElement, extending from one of the provided base classes, and implement one of provided DSL test element interfaces.
   * In some cases, a test element is not yet supported by the DSL, but it is highly related to some other JMeter test element, and its functionality can be thought of just as an extension of existing support. In such cases, evaluate if the new feature should be added to the existing DslTestElement, or if is it worth creating a separate DSL test element for it. E.g: [DslCsvDataSet](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/configs/DslCsvDataSet.java) contains both support for standard [JMeter Data CSV element](https://jmeter.apache.org/usermanual/component_reference.html#CSV_Data_Set_Config) and, through the usage of `randomOrder` method, usage of [Random CSV data set element](https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/random-csv-data-set/RandomCSVDataSetConfig.md) (which is provided by a separate JMeter plugin). This eases general usage of DSL, allowing to easily identify variants of a DSL test element, not requiring previous knowledge of existing plugins or particular details of each JMeter test element.
3. Evaluate if a new maven module is needed.
   * If a new DSL test element is needed, and its functionality is not part of JMeter core features and is not a common feature of most common test plans, then consider creating a separate module for it. Creating a separate module avoids the unnecessary inclusion of dependencies when the feature is not needed, in addition to separating builder methods from core ones.
   * Use short but descriptive module names for easy inclusion in users' projects and identification.
4. Implement a new DSL Test element
   * Use descriptive naming in the new class and package. If the name collides with JMeter class names or concepts, then consider prepending `Dsl` to it.
   * Extend from one of the existing base classes (like [BaseSampler], [BaseConfigElement], etc.) or extend from `BaseTestElement` and proper interface for the test element (like [DslTimer], [DslPostProcessor], etc).
   * Add proper java doc, explaining the feature implemented by the DSL new test element. Include the `@since` tag with the next minor version of the DSL (ie: if the current version is `0.44`, then use `0.45`).
   * Consider each instance of a DslTestElement as potentially creating multiple instances of the same sampler (or as if `buildTestElement` could be invoked several times without affecting the status of the element). In particular, avoid storing the already built test element in the DSL test element, and only create it in `buildTestElement` (and child methods).
   * Consider DslTestElement fields that might be interesting for advanced users and framework developers usage (eg: to alter an existing test plan, or query settings about it) for protected visibility instead of private. In the same fashion, for such fields, avoid them being marked as final. This allows for greater flexibility of usage of the DSL while not polluting the user API with methods that they usually would not use.
   * Provide methods to configure optional aspects of the test element, using as default values the most common scenarios and returning the DslTestElement instance for fluent API configuration.
      * Use short but well-descriptive names for such methods, so they are easy to type and read/identify.
      * Prefer using nouns for these methods' names.
      * Consider names that are easy to recognize for JMeter users, but don't feel tied to JMeter terminology. Prefer using a well-describing name than matching JMeter naming.
      * In general, each method should accept one parameter (the JMeter property value).
      * Use `String` type of parameters if you want to support JMeter expressions (e.g: JMeter variables). But more importantly, consider providing type-safe parameters (eg: `int` type parameter) to properly reflect the nature and expected values for each property.
      * Consider creating nested enums for properties that only accept a limited set of values. E.g. `HttpClientImpl` in [DslHttpSampler]
      * In some cases, you might want to abstract common settings that would otherwise require multiple methods invocations. E.g.: `post`, `header` and `contentType` methods in [DslHttpSampler].
      * Include javadoc in the method explaining when it should be used (its purpose).
5. Implement DSL builder methods
   * If a new DSL test element is needed, then you will need to implement some static public builder/factory methods for it, for easy usage in DSL test plans through static imports.
   * Location:
      * If the test element is part of the core module, just add builder methods to [JmeterDsl] class.
      * If you use a separate module, then, if you have several test elements on the module, consider creating or adding the builder methods to a DSL class for that module (eg: [JdbcJmeterDsl](jmeter-java-dsl-jdbc/src/main/java/us/abstracta/jmeter/javadsl/jdbc/JdbcJmeterDsl.java)), as a way to easily discover all methods for that module.
      * If you have a single test element on the module, then you can directly add the builder methods to the DSL test element class, to keep things simple (eg: [ParallelController](jmeter-java-dsl-parallel/src/main/java/us/abstracta/jmeter/javadsl/parallel/ParallelController.java)).
   * Conventions:
      * Use short but well-descriptive names for builder methods. It is important to use names that are easy to recognize for JMeter users, but also evaluate if there is a better/more describing name for the feature than the one already provided to the JMeter element.
      * Use nouns for these methods, which properly describe the DslTestElement.
      * Keep the builder methods short, to ease typing and recognition, but at the same time, keep them descriptive (avoid acronyms, unless they are super intuitive/popular).
      * Include javadoc explaining the main purpose of the built element, and when this builder should be used over other builders for the same DslTestElement. Additionally, add link (with `@see` annotation) to the created DslTestElement for further details about the new element and further configuration.
      * Provide a builder method for each set of required parameter combinations.
      * If the test element name/label is meaningful for the test element (i.e.: affects collected statistics), then consider adding builder methods with it as the first parameter. You can provide both methods with names and without them if names are optional.
      * If the element is a natural container of other elements (ie: controller & thread group), then consider providing a builder method that accepts a var args of children elements.
      * If the element is a container of elements and has some optional configuration, then consider creating the builder method with just the required parameters, implement methods in the DslTestElement class for optional parameters, and a `children` method for specifying children elements.
      * In general, when using an element in code it should look like some of these examples:

        ```java
          myElement(elemName, requiredPropVal)
              .optionalProp(propVal)
              .children(
                child
              )
        
          myElement(requiredPropVal)
              .optionalProp(propVal)
              .children(
                child
              )
        
          myElement(requiredPropVal, child)
        
          myElement(elemName, requiredPropVal, child)
        ```
6. Implement tests that verify the expected behavior of the test element in a test plan execution. This way, you verify that you properly initialize JMeter properties and that your interpretation of the test element properties and behavior is right.
   * Check [DslHttpSamplerTest](jmeter-java-dsl/src/test/java/us/abstracta/jmeter/javadsl/http/DslHttpSamplerTest.java) for some sample test cases.
7. Add a nested `CodeBuilder` class extending [SingleTestElementCodeBuilder], [SingleGuiClassCodeBuilder] or if neither of the two is enough, then consider extending [MethodCallBuilder].
   * Implement the necessary logic to generate DSL code from the JMeter test element.
   * Check [DslTestPlan] for an example code builder and [MethodCallBuilder] for additional details on how to implement them.
   * Implement tests for the code builder.
      * The DSL provides abstract class [MethodCallBuilderTest](jmeter-java-dsl/src/test/java/us/abstracta/jmeter/javadsl/codegeneration/MethodCallBuilderTest.java) that provides common logic for testing builders.
      * MethodCallBuilderTest included logic gets all methods in the MethodCallBuilderTest subclass that return a DslTestPlan instance, generates a JMX for each of them, generates DSL code for each of generated JMX, and compares the test code to the generated one, which should be the same.
      * In MethodCallBuilderTest defined tests you should not use methods for abstracting duplicate logic between tests (nor variables or constants for literals). Code duplication in this case is acceptable and expected.
      * Check [DslTestPlanTest](jmeter-java-dsl/src/test/java/us/abstracta/jmeter/javadsl/core/DslTestPlanTest.java) for some tests examples.
8. Run `mvn clean package` and fix any potential code styling issues or failing tests.
9. Add a new section [user guide](docs/guide), by adding a new md file and proper `@include` in parent section, describing the new feature. Consider running in the `docs` directory `yarn install` and `yarn dev` (this requires node 14 and yarn installed on your machine) to run a local server for docs, where you can review that new changes are properly showing.
10. Commit changes to git, using as a comment a subject line that describes general changes, and if necessary, some additional details describing the reason why the change is necessary.
11. Submit a pull request to the repository including a meaningful name.
12. Check GitHub Actions execution to verify that no test fails on the CI pipeline.
13. When the PR is merged and release is triggered. Enjoy the pleasure and the pride of contributing with an OSS tool :).

## General coding guidelines

* Review existing code, and get a general idea of main classes & conventions. It is important to try to keep code consistent to ease maintenance.
* In general, avoid code duplication to ease maintenance and readability.
* Use meaningful names for variables, methods, classes, etc. Avoid acronyms unless they are super intuitive.
* Strive for simplicity and reduce code and complexity whenever possible. Avoid over-engineering (implementing things for potential future scenarios).
* Use comments to describe the reason for some code (the "why"), either because is not the natural/obvious expected code, or because additional clarification is needed. Do not describe the "what". You can use variables, methods & class names to describe the "what".
* Provide javadocs for all public classes and methods that help users understand when to use the method, test element, etc.
* Avoid leaving `TODO` comments in the code. Create an issue in the GitHub repository, discussion, or include some comment in PR instead.
* Don't leave dead code (commented-out code).
* Avoid including backward incompatible changes (unless required), that would require users to change existing code where they use the API.
* Be gentle and thoughtful when you review code, contribute and submit pull requests :).

## FAQ

### I want to add support for a new protocol (e.g.: HTTP2) or feature provided by a JMeter plugin. How should I proceed?

First, you will probably need a new maven module adding the proper plugin and libraries dependencies required to support the new functionality.

After that, you need to create a new package (e.g.: us.abstracta.jmeter.javadsl.http2) containing a new class (e.g.: Http2JmeterDsl) that provides factory methods for the test elements of the protocol or plugin.

Additionally, you need to create new [DslTestElement] classes to implement the logic for each of the new test elements (e.g. Http2Sampler). Check [Core classes section] and the following item for some guidelines.

Remember to add proper tests and some documentation so users know about the new features!

### I want to add a new JMeter standard (sampler, listener, pre/postprocessor, assertion, etc.) test element. What should I do?

When adding a new test element, a new [DslTestElement] class is required to handle the specific attributes and construction of the JMeter test element, and a new factory method in [JmeterDsl] needs to be added to instantiate this class.

In general, check existing classes extending [DslTestElement] and factory methods of [JmeterDsl] looking for any similarities with the required test element, and take examples from them.

Consider when creating the [DslTestElement] class, the proper interface to implement according to where the test element can be located within a test plan (as previously described in [Core classes section]).

Add proper tests and some documentation so users know about the new features!

### I want to add support for a new attribute for an already supported test element. Any guides?

Think if the attribute is an attribute that will be required in most of the cases and is required for the test element specification, or if it is an optional one.

If the attribute is a "required" one, then consider adding a new factory method to [JmeterDsl] containing the new parameter, and evaluate if it makes sense to introduce a non-backward compatible change to existing factory methods that don't contain the attribute. Additionally, locate the correct [DslTestElement] class and modify it to set the proper JMeter test element property.

If the attribute is optional, then locate the correct [DslTestElement] class and add to it a new method for the attribute and modify the class logic to set the associated JMeter test element property.

Add proper tests and update documentation (if needed) so users know about the new features!

### I don't understand and still don't know how to implement what I need. What can I do?

Just create an issue in the repository stating what you need and why, and we will do our best to implement what you need :).

Or, check existing code. It contains embedded documentation with additional details, and the code never lies.

[JmeterDsl]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/JmeterDsl.java
[DslTestPlan]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslTestPlan.java
[DslTestElement]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslTestElement.java
[BaseSampler]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/samplers/BaseSampler.java
[BaseConfigElement]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/configs/BaseConfigElement.java
[DslHttpSampler]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/http/DslHttpSampler.java
[TestElementContainer]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/testelements/TestElementContainer.java
[DslTimer]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/timers/DslTimer.java
[DslPostProcessor]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslPostProcessor.java
[HttpHeaders]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/http/HttpHeaders.java
[JtlWriter]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/listeners/JtlWriter.java
[MethodCallBuilder]:jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/codegeneration/MethodCallBuilder.java
[SingleTestElementCallBuilder]:jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/codegeneration/SingleTestElementCallBuilder.java
[SingleGuiClassCallBuilder]:jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/codegeneration/SingleGuiClassCallBuilder.java
[Core classes section]: #core-classes
