# unidev-prometheus

Library to expose Prometheus metrics

# Usage

```
...
repositories {
    maven {
        url "https://mvn.universal-development.com/public" 
    }
}
...
dependencies {
	implementation('com.unidev.unidev-prometheus:unidev-prometheus:0.0.1')
}

```

Usage:
```
MetricService metricService = new MetricService(ConfigService.appName(), prometheusPort);

...

metricService.inc("failed-operation", Map.of("type", "read-db"));

metricService.counter("template-evaluation", Map.of("template", templateName)).inc();

try {
...
} catch(Throwable t) {
  metricService.countException(t, Map.of("method", "dbQuery()"));
}

```


## References

https://github.com/prometheus/client_java#instrumenting

https://github.com/sysdiglabs/custom-metrics-examples/blob/master/prometheus/java/src/main/java/Main.java

https://sysdig.com/blog/prometheus-metrics/


## License

This code is released under the MIT License. See LICENSE.
