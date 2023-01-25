### Thread groups order

By default, when you add multiple thread groups to a test plan, JMeter will run them all in parallel. This is a very helpful behavior in many cases, but in some others, you may want to run them sequentially (one after the other). To achieve this you can just use `sequentialThreadGroups()` test plan method.
