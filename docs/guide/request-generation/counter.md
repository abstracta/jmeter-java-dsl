### Counter

In scenarios that you need unique value for each request, for example for id parameters, you can use `counter` which provides easy means to have an auto incremental value that can be used in requests.

Here is an example:

```java
testPlan(
    threadGroup(1, 10,
        counter("USER_ID")
            .startingValue(1000), // will generate 1000, 1001, 1002...
        httpSampler(wiremockUri + "/${USER_ID}")
    )
).run();
```

Check [DslCounter](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/configs/DslCounter.java) for more details.
