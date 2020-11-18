# Contributing

This guide will introduce you to the code to help you understand better how it works and send pull requests to do your own contributions.

Before continuing, if you are not familiar with JMeter, please review [JMeter test plan elements](https://jmeter.apache.org/usermanual/test_plan.html) for basic understanding of JMeter core concepts.

Let's start looking at each of the main classes, and later on, show a diagram to get an overview of their relations.

## Core classes

[JmeterDsl] is the main entry point of the library and provides factory methods that allow to create a test plan and run it. Each factory method receive as parameters the main attributes of a test element, which are required in most cases, and, when it is a natural container of other test elements (i.e.: it's of no use when no included children), the list of children test elements to nest on it (eg: thread groups to put on a test plan).

Test elements are classes that implement [DslTestElement] interface, which basically setup JMeter test elements to be included in a test plan. They might also provide fluent api methods to set optional test element attributes (eg: http method for http sampler) or add children to them. 

To implement your own DslTestElement, the library already provides some base classes from where to start:
 
* [BaseTestElement](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/BaseTestElement.java): This contains the very basics of test elements, allowing to abstract the common logic of all test elements (ie: naming them, setting common properties and adding them to a JMeter tree). Create a subclass of this class only if the test element can't nest children, otherwise extend [TestElementContainer]. Examples: [HttpHeaders] and [JtlWriter].
* [TestElementContainer]: This represents a test element which can nest other test elements. If you need to implement a Sampler, then extend DslSampler instead. Examples: [DslTestPlan](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslTestPlan.java), [DslThreadGroup](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslThreadGroup.java).
* [DslSampler]: This contains common logic for samplers, and should be the class extended in most of the cases. Examples: [DslHttpSampler](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/http/DslHttpSampler.java)

As previously mentioned, DslTestElement instances can be nested, and when implementing one, you should define under what other test elements this element can be nested (eg: thread groups can only be added under test plans). To do that, just implement the appropriate interface required by the associated "parent" element, currently provided interfaces are: 

* [TestPlanChild](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslTestPlan.java)
* [ThreadGroupChild](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslThreadGroup.java)
* [SamplerChild](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslSampler.java)

So, for example, [DslThreadGroup](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslThreadGroup.java) implements TestPlanChild and [DslSampler] implements ThreadGroupChild. You might implement multiple interfaces if the element can be used at different levels, for example check [HttpHeaders] and [JtlWriter] implement multiple interfaces.

You might also implement a new "child" interface if you need additional hierarchy levels (like ControllerChild).

## Test runs

When you want to run a test plan, it needs to run in a JMeter engine. By default, DslTestPlan uses [EmbeddedJMeterEngine](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/EmbeddedJmeterEngine.java), which is the fastest and easiest way to run a test plan, but you might use EmbeddedJMeterEngine as an example and implement your own engine (for example to run tests on BlazeMeter, or in a distributed fashion). 

When a test plan runs, the engine returns an instance of [TestPlanStats](jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/TestPlanStats.java), grouping information by test element name (aka label). This allows to check the expected statistics and verify that everything worked within expected boundaries.

## Class diagram

![image](http://www.plantuml.com/plantuml/png/jLNDRjim3BxhAOYUdA7k8nJ5kcv5WG45sc67eWV5ucAjacnGzB0YsBlFYYE4yS-oOjZ994D-_2Z-v2go9kwyKog-sD1gqXvy4vggfiOZC7MeQciGqBVy2Xxz6YaEfifIvL2fokaOuSuZ8ts83hOAeFy-OkHxfmFmNYYovRgspZmzGvM-X66r3wW9jVb4JTS27-J21jxhyHoIOXb9isr2hukWDy8-CDcmWQ0zdiC_vhXbRX-qRnqIr0Svv-Z8dNGKP8wZKiWjQeCKeJx8HVhjZjrNIbJXUvOoL6_uQ1skr2-bMbszw0r2rH32LYb89I9zqnNcJjquz1Xurxm-fuxr8o72kklhJDANO7hiR8TM4mPE57fu0BsEJBfY66RXlOueGQrl1odEfnFLxtJXCJ0GXc8fgV2vRW9h6v00Vp86CsMqpI13h1ZN73AFD_3DlW-iEG9r8PEI3tiqxHarPrhWSUFNQ0oQOPbkmgPFkfXpRiTMaxh4yFwnDD6a4VKXlzYGh8ibcPOlyfv-WMaxarjUbB-aFKsvdTBQOxwXANd9bgTfyUccf-DS4iXFUxonzvhhWO3OtBJui9MDbQT45f9D6JgVafsQOZuZruq1l_j8ftLZNfC-1c-Ro8NmMSqv3N359C6iSq8vYLYZDWkNyOTs7Su6vRjSx0tJUorRAs_h_sgkJ76wfDptmxzf-LHROp0ybxlphMQNeEpQyXBEtfsDHbd96t5nSNbSWHgTX-Gmhcc2C7UYb9Yxv2EVGOqwL9QNyasKki9WEkz-0m00)

## General Guidelines

* When submitting changes follow same conventions followed in rest of the code, and specify clear intention of why are the changes required.
* Avoid including backward incompatible changes (unless required), that would require users changing existing code where they use the api. 
* Avoid using names already taken by JMeter for classes to avoid confusion (eg: instead of using TestPlan use DslTestPlan).
* For any changes include appropriate automated tests.
* Update documentation if necessary.

## FAQ

### I want to add support for a new protocol (e.g.: HTTP2) or feature provided by a JMeter plugin. How should I proceed?

First, you will need to add an optional dependency to the [pom.xml](/pom.xml) to include the plugin and libraries required to support the new functionality.

After that, you need to create a new package (e.g.: us.abstracta.jmeter.javadsl.http2) containing a new class (e.g.: Http2JMeterDsl) that provides factory methods for the test elements of the protocol or plugin. 

Additionally, you need to create new [DslTestElement] classes to implement the logic for each of the new test elements (e.g. Http2Sampler). Check [Core classes section] and following item for some guidelines.

Remember adding proper tests and some documentation so users know about the new features! 

### I want to add a new JMeter standard (sampler, listener, pre/post processor, assertion, etc) test element. What should I do?

When adding a new test element, a new [DslTestElement] class is required to handle the specific attributes and construction of the JMeter test element and a new factory method in [JmeterDsl] needs to be added to instantiate this class.

In general, check existing classes extending [DslTestElement] and factory methods of [JmeterDsl] looking for any similarities with required test element, and take examples from them.

Consider when creating the [DslTestElement] class, the proper interface to implement according to where the test element can be located within a test plan (as previously described in [Core classes section]).

Add proper tests and some documentation so users know about the new features!

### I want to add support for a new attribute for an already supported test element. Any guides?

Think if the attribute is an attribute that will be required in most of the cases and is required for the test element specification, or if it is an optional one. 

If the attribute is a "required" one, then consider adding a new factory method to [JmeterDsl] containing the new parameter, and evaluate if makes sense to introduce a non backward compatible change to existing factory methods which don't contain the attribute. Additionally, locate the correct [DslTestElement] class and modify it to set the proper JMeter test element property. 

If the attribute is optional, then locate the correct [DslTestElement] class and add to it a new method for the attribute and modify the class logic to set the associated JMeter test element property. 

Add proper tests and update documentation (if needed) so users know about the new features!

### I don't understand and still don't know how to implement what I need. What can I do?

Just create an issue in the repository stating what you need and why, and we will do our best to implement what you need :).

Or, check existing code. It contains embedded documentation with additional details, and the code never lies.

[JmeterDsl]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/JmeterDsl.java
[DslTestElement]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslTestElement.java
[TestElementContainer]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/TestElementContainer.java
[HttpHeaders]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/http/HttpHeaders.java
[DslSampler]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslSampler.java
[JtlWriter]: jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/listeners/JtlWriter.java
[Core classes section]: #core-classes
