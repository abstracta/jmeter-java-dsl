### Real-time metrics visualization and historic data storage

When running tests with JMeter (and in particular with jmeter-java-dsl) a usual requirement is to be able to store such test runs in a persistent database to, later on, review such metrics, and compare different test runs. Additionally, jmeter-java-dsl only provides some summary data of a test run in the console while it is running, but, since it doesn't provide any sort of UI, this doesn't allow you to easily analyze such information as it can be done in JMeter GUI.

<!-- @include: influxdb.md -->
<!-- @include: elasticsearch.md -->
<!-- @include: datadog.md -->
