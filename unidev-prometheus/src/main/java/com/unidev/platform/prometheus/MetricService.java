package com.unidev.platform.prometheus;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Collections.emptyMap;

/**
 * Service used to track metrics
 */
public class MetricService {
    private final HTTPServer server;

    private final String prefix;

    private final Map<String, String> defaultLabels;

    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Gauge> gauges = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Summary> summaries = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Histogram> histograms = new ConcurrentHashMap<>();

    public MetricService(int prometheusPort) throws IOException {
        this("", prometheusPort, emptyMap());
    }

    public MetricService(String prefix, int prometheusPort) throws IOException {
        this(prefix, prometheusPort, emptyMap());
    }

    public MetricService(String prefix, int prometheusPort, Map<String, String> defaultLabels) throws IOException {
        this.prefix = prefix;
        DefaultExports.initialize();
        this.server = new HTTPServer.Builder()
                .withPort(prometheusPort)
                .build();
        this.defaultLabels = defaultLabels;
    }

    /**
     * Register counter
     */
    public Counter.Child counter(String name, Map<String, String> labels) {
        try {
            TreeMap<String, String> labelMap = new TreeMap<>(labels);
            labelMap.putAll(defaultLabels);
            String prometheusName = buildName(name);
            counters.computeIfAbsent(prometheusName, s -> {
                Counter value = Counter.build()
                        .name(prometheusName)
                        .labelNames(mapKeys(labelMap))
                        .help("Counter " + name).register();
                return value;
            });
            return counters.get(prometheusName).labels(mapValues(labelMap));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new Counter.Child();
    }

    /**
     * Increment counter
     */
    public void inc(String name, Map<String, String> labels) {
        counter(name, labels).inc();
    }

    /**
     * Register Gauge
     */
    public Gauge.Child gauge(String name, Map<String, String> labels) {
        TreeMap<String, String> labelMap = new TreeMap<>(labels);
        labelMap.putAll(defaultLabels);
        try {
            String prometheusName = buildName(name);
            gauges.computeIfAbsent(prometheusName, s -> {
                Gauge value = Gauge.build()
                        .name(prometheusName)
                        .help("Gauge " + prometheusName)
                        .labelNames(mapKeys(labelMap))
                        .register();
                return value;
            });
            return gauges.get(prometheusName).labels(mapValues(labelMap));
        }catch (Throwable t) {
            t.printStackTrace();
        }
        return Gauge.build().create().labels();
    }

    /**
     * Build summary
     */
    public Summary.Child summary(String name, Map<String, String> labels) {
        TreeMap<String, String> labelMap = new TreeMap<>(labels);
        labelMap.putAll(defaultLabels);
        try {
            String prometheusName = buildName(name);
            summaries.computeIfAbsent(prometheusName, s -> {
                Summary value = Summary.build()
                        .name(prometheusName)
                        .help("Summary " + prometheusName)
                        .labelNames(mapKeys(labelMap))
                        .register();
                return value;
            });
            return summaries.get(prometheusName).labels(mapValues(labelMap));
        }catch (Throwable t) {
            t.printStackTrace();
        }
        return Summary.build().create().labels(mapValues(labelMap));
    }

    /**
     * Register histogram
     */
    public Histogram.Child histogram(String name, Map<String, String> labels) {
        TreeMap<String, String> labelMap = new TreeMap<>(labels);
        labelMap.putAll(defaultLabels);
        try {
            String prometheusName = buildName(name);
            histograms.computeIfAbsent(prometheusName, s -> {
                Histogram value = Histogram.build()
                        .name(prometheusName)
                        .help("Histogram " + prometheusName)
                        .labelNames(mapKeys(labelMap))
                        .register();
                return value;
            });
            return histograms.get(prometheusName).labels(mapValues(labelMap));
        }catch (Throwable t) {
            t.printStackTrace();
        }
        return Histogram.build().create().labels(mapValues(labelMap));
    }

    /**
     * Count exceptions.
     */
    public void countException(Throwable t, Map<String, String> labels) {
        TreeMap<String, String> labelMap = new TreeMap<>(labels);
        labelMap.putAll(defaultLabels);
        String name = t.getClass().getCanonicalName();
        labelMap.put("exception", name);
        counter("exception", labelMap).inc();
    }

    String[] mapKeys(Map<String, String> map) {
        return map.keySet().toArray(new String[0]);
    }

    String[] mapValues(Map<String, String> map) {
        return map.values().toArray(new String[0]);
    }

     String buildName(String name) {
        if (prefix == null || prefix.isEmpty()) {
            return name.toLowerCase().replaceAll("[^A-Za-z0-9]", "_");
        }
        return (prefix + "_" + name.toLowerCase()).replaceAll("[^A-Za-z0-9]", "_");
    }

    public void stop() {
        server.close();
    }

}
