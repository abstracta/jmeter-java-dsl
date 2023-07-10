#### Timeouts

By default, JMeter uses system default configurations for connection and response timeouts (maximum time for a connection to be established or a server response after a request, before it fails). This is might make the test behave different depending on the machine where it runs. To avoid this, it is recommended to always set these values. Here is an example:

```java
testPlan(
    httpDefaults()
        .connectionTimeout(Duration.ofSeconds(10))
        .responseTimeout(Duration.ofMinutes(1)),
    threadGroup(2, 10,
        httpSampler("http://my.service")
    )
)
```

::: warning
Currently we are using same defaults as JMeter to avoid breaking existing test plans executions, but in a future major version we plan to change default setting to avoid the common pitfall previously mentioned.
:::
