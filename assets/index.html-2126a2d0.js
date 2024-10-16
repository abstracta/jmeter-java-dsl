const e=JSON.parse('{"key":"v-fffb8e28","path":"/guide/","title":"User guide","lang":"en-US","frontmatter":{},"headers":[{"level":2,"title":"Setup","slug":"setup","link":"#setup","children":[]},{"level":2,"title":"Simple HTTP test plan","slug":"simple-http-test-plan","link":"#simple-http-test-plan","children":[]},{"level":2,"title":"DSL recorder","slug":"dsl-recorder","link":"#dsl-recorder","children":[{"level":3,"title":"Correlations","slug":"correlations","link":"#correlations","children":[]}]},{"level":2,"title":"DSL code generation from JMX file","slug":"dsl-code-generation-from-jmx-file","link":"#dsl-code-generation-from-jmx-file","children":[]},{"level":2,"title":"Run test at scale","slug":"run-test-at-scale","link":"#run-test-at-scale","children":[{"level":3,"title":"BlazeMeter","slug":"blazemeter","link":"#blazemeter","children":[]},{"level":3,"title":"OctoPerf","slug":"octoperf","link":"#octoperf","children":[]},{"level":3,"title":"Azure Load Testing","slug":"azure-load-testing","link":"#azure-load-testing","children":[]},{"level":3,"title":"JMeter remote testing","slug":"jmeter-remote-testing","link":"#jmeter-remote-testing","children":[]}]},{"level":2,"title":"Auto Stop","slug":"auto-stop","link":"#auto-stop","children":[]},{"level":2,"title":"Advanced threads configuration","slug":"advanced-threads-configuration","link":"#advanced-threads-configuration","children":[{"level":3,"title":"Thread ramps and holds","slug":"thread-ramps-and-holds","link":"#thread-ramps-and-holds","children":[]},{"level":3,"title":"Throughput based thread group","slug":"throughput-based-thread-group","link":"#throughput-based-thread-group","children":[]},{"level":3,"title":"Set up & tear down","slug":"set-up-tear-down","link":"#set-up-tear-down","children":[]},{"level":3,"title":"Thread groups order","slug":"thread-groups-order","link":"#thread-groups-order","children":[]}]},{"level":2,"title":"Test plan debugging","slug":"test-plan-debugging","link":"#test-plan-debugging","children":[{"level":3,"title":"View results tree","slug":"view-results-tree","link":"#view-results-tree","children":[]},{"level":3,"title":"Post-processor breakpoints","slug":"post-processor-breakpoints","link":"#post-processor-breakpoints","children":[]},{"level":3,"title":"Debug info during test plan execution","slug":"debug-info-during-test-plan-execution","link":"#debug-info-during-test-plan-execution","children":[]},{"level":3,"title":"Debug JMeter code","slug":"debug-jmeter-code","link":"#debug-jmeter-code","children":[]},{"level":3,"title":"Debug Groovy code","slug":"debug-groovy-code","link":"#debug-groovy-code","children":[]},{"level":3,"title":"Dummy sampler","slug":"dummy-sampler","link":"#dummy-sampler","children":[]},{"level":3,"title":"Test plan review in JMeter GUI","slug":"test-plan-review-in-jmeter-gui","link":"#test-plan-review-in-jmeter-gui","children":[]}]},{"level":2,"title":"Reporting","slug":"reporting","link":"#reporting","children":[{"level":3,"title":"Log requests and responses","slug":"log-requests-and-responses","link":"#log-requests-and-responses","children":[]},{"level":3,"title":"Real-time metrics visualization and historic data storage","slug":"real-time-metrics-visualization-and-historic-data-storage","link":"#real-time-metrics-visualization-and-historic-data-storage","children":[{"level":4,"title":"InfluxDB","slug":"influxdb","link":"#influxdb","children":[]},{"level":4,"title":"Graphite","slug":"graphite","link":"#graphite","children":[]},{"level":4,"title":"Elasticsearch","slug":"elasticsearch","link":"#elasticsearch","children":[]},{"level":4,"title":"Prometheus","slug":"prometheus","link":"#prometheus","children":[]},{"level":4,"title":"DataDog","slug":"datadog","link":"#datadog","children":[]}]},{"level":3,"title":"Generate HTML reports from test plan execution","slug":"generate-html-reports-from-test-plan-execution","link":"#generate-html-reports-from-test-plan-execution","children":[]},{"level":3,"title":"Live built-in graphs and stats","slug":"live-built-in-graphs-and-stats","link":"#live-built-in-graphs-and-stats","children":[]}]},{"level":2,"title":"Response processing","slug":"response-processing","link":"#response-processing","children":[{"level":3,"title":"Check for expected response","slug":"check-for-expected-response","link":"#check-for-expected-response","children":[]},{"level":3,"title":"Check for expected JSON","slug":"check-for-expected-json","link":"#check-for-expected-json","children":[]},{"level":3,"title":"Change sample result statuses with custom logic","slug":"change-sample-result-statuses-with-custom-logic","link":"#change-sample-result-statuses-with-custom-logic","children":[{"level":4,"title":"Lambdas","slug":"lambdas","link":"#lambdas","children":[]}]},{"level":3,"title":"Use part of a response in a subsequent request (aka correlation)","slug":"use-part-of-a-response-in-a-subsequent-request-aka-correlation","link":"#use-part-of-a-response-in-a-subsequent-request-aka-correlation","children":[{"level":4,"title":"Regular expressions extraction","slug":"regular-expressions-extraction","link":"#regular-expressions-extraction","children":[]},{"level":4,"title":"Boundaries based extraction","slug":"boundaries-based-extraction","link":"#boundaries-based-extraction","children":[]},{"level":4,"title":"JSON extraction","slug":"json-extraction","link":"#json-extraction","children":[]}]}]},{"level":2,"title":"Requests generation","slug":"requests-generation","link":"#requests-generation","children":[{"level":3,"title":"Conditionals","slug":"conditionals","link":"#conditionals","children":[]},{"level":3,"title":"Loops","slug":"loops","link":"#loops","children":[{"level":4,"title":"Iterating over extracted values","slug":"iterating-over-extracted-values","link":"#iterating-over-extracted-values","children":[]},{"level":4,"title":"Iterating while a condition is met","slug":"iterating-while-a-condition-is-met","link":"#iterating-while-a-condition-is-met","children":[]},{"level":4,"title":"Iterating a fixed number of times","slug":"iterating-a-fixed-number-of-times","link":"#iterating-a-fixed-number-of-times","children":[]},{"level":4,"title":"Iterating for a given period","slug":"iterating-for-a-given-period","link":"#iterating-for-a-given-period","children":[]},{"level":4,"title":"Execute only once in thread","slug":"execute-only-once-in-thread","link":"#execute-only-once-in-thread","children":[]}]},{"level":3,"title":"Group requests","slug":"group-requests","link":"#group-requests","children":[]},{"level":3,"title":"CSV as input data for requests","slug":"csv-as-input-data-for-requests","link":"#csv-as-input-data-for-requests","children":[]},{"level":3,"title":"Counter","slug":"counter","link":"#counter","children":[]},{"level":3,"title":"Provide request parameters programmatically per request","slug":"provide-request-parameters-programmatically-per-request","link":"#provide-request-parameters-programmatically-per-request","children":[]},{"level":3,"title":"Timers","slug":"timers","link":"#timers","children":[{"level":4,"title":"Emulate user delays between requests","slug":"emulate-user-delays-between-requests","link":"#emulate-user-delays-between-requests","children":[]},{"level":4,"title":"Control throughput","slug":"control-throughput","link":"#control-throughput","children":[]},{"level":4,"title":"Requests synchronization","slug":"requests-synchronization","link":"#requests-synchronization","children":[]}]},{"level":3,"title":"Execute part of a test plan part a fraction of the times","slug":"execute-part-of-a-test-plan-part-a-fraction-of-the-times","link":"#execute-part-of-a-test-plan-part-a-fraction-of-the-times","children":[]},{"level":3,"title":"Switch between test plan parts with a given probability","slug":"switch-between-test-plan-parts-with-a-given-probability","link":"#switch-between-test-plan-parts-with-a-given-probability","children":[]},{"level":3,"title":"Parallel requests","slug":"parallel-requests","link":"#parallel-requests","children":[]}]},{"level":2,"title":"JMeter variables & properties","slug":"jmeter-variables-properties","link":"#jmeter-variables-properties","children":[{"level":3,"title":"Variables","slug":"variables","link":"#variables","children":[]},{"level":3,"title":"Properties","slug":"properties","link":"#properties","children":[]}]},{"level":2,"title":"Test resources","slug":"test-resources","link":"#test-resources","children":[]},{"level":2,"title":"Protocols","slug":"protocols","link":"#protocols","children":[{"level":3,"title":"HTTP","slug":"http","link":"#http","children":[{"level":4,"title":"Methods & body","slug":"methods-body","link":"#methods-body","children":[]},{"level":4,"title":"Parameters","slug":"parameters","link":"#parameters","children":[]},{"level":4,"title":"Headers","slug":"headers","link":"#headers","children":[]},{"level":4,"title":"Authentication","slug":"authentication","link":"#authentication","children":[]},{"level":4,"title":"Multipart requests","slug":"multipart-requests","link":"#multipart-requests","children":[]},{"level":4,"title":"Cookies & caching","slug":"cookies-caching","link":"#cookies-caching","children":[]},{"level":4,"title":"Timeouts","slug":"timeouts","link":"#timeouts","children":[]},{"level":4,"title":"Connections","slug":"connections","link":"#connections","children":[]},{"level":4,"title":"Embedded resources","slug":"embedded-resources","link":"#embedded-resources","children":[]},{"level":4,"title":"Redirects","slug":"redirects","link":"#redirects","children":[]},{"level":4,"title":"HTTP defaults","slug":"http-defaults","link":"#http-defaults","children":[]},{"level":4,"title":"Overriding URL protocol, host or port","slug":"overriding-url-protocol-host-or-port","link":"#overriding-url-protocol-host-or-port","children":[]},{"level":4,"title":"Proxy","slug":"proxy","link":"#proxy","children":[]}]},{"level":3,"title":"GraphQL","slug":"graphql","link":"#graphql","children":[]},{"level":3,"title":"JDBC and databases interactions","slug":"jdbc-and-databases-interactions","link":"#jdbc-and-databases-interactions","children":[]},{"level":3,"title":"Java API performance testing","slug":"java-api-performance-testing","link":"#java-api-performance-testing","children":[]},{"level":3,"title":"Selenium","slug":"selenium","link":"#selenium","children":[]}]},{"level":2,"title":"Custom or yet not supported test elements","slug":"custom-or-yet-not-supported-test-elements","link":"#custom-or-yet-not-supported-test-elements","children":[]},{"level":2,"title":"JMX support","slug":"jmx-support","link":"#jmx-support","children":[{"level":3,"title":"Save as JMX","slug":"save-as-jmx","link":"#save-as-jmx","children":[]},{"level":3,"title":"Run JMX file","slug":"run-jmx-file","link":"#run-jmx-file","children":[]}]}],"git":{},"filePathRelative":"guide/index.md"}');export{e as data};
