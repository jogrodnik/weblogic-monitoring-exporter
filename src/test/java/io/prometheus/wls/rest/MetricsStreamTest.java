package io.prometheus.wls.rest;
/*
 * Copyright (c) 2017 Oracle and/or its affiliates
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static io.prometheus.wls.rest.matchers.PrometheusMetricsMatcher.followsPrometheusRules;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * @author Russell Gold
 */
public class MetricsStreamTest {
    private static final long NANOSEC_PER_SECONDS = 1000000000;
    private PerformanceProbeStub performanceProbe = new PerformanceProbeStub();
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private PrintStream ps = new PrintStream(baos);
    private MetricsStream metrics = new MetricsStream(ps, performanceProbe);

    @Before
    public void setUp() {
        LiveConfiguration.setServer("localhost", 7001);
    }

    @Test
    public void whenNoMetricsScraped_reportNoneScraped() {
        assertThat(getPrintedMetrics(),
                containsString("wls_scrape_mbeans_count_total{instance=\"localhost:7001\"} 0"));
    }

    private String getPrintedMetrics() {
        metrics.printPerformanceMetrics();
        return baos.toString();
    }

    @Test
    public void afterMetricsScraped_reportScrapedCount() {
        metrics.printMetric("a", 12);
        metrics.printMetric("b", 120);
        metrics.printMetric("c", 0);

        assertThat(getPrintedMetrics(),
                containsString("wls_scrape_mbeans_count_total{instance=\"localhost:7001\"} 3"));
    }

    @Test
    public void afterTimePasses_reportScrapeDuration() {
        performanceProbe.incrementElapsedTime(12.4);

        assertThat(getPrintedMetrics(),
                containsString("wls_scrape_duration_seconds{instance=\"localhost:7001\"} 12.40"));
    }

    @Test
    public void afterProcessing_reportCpuPercent() {
        performanceProbe.incrementCpuTime(3.2);

        assertThat(getPrintedMetrics(),
                containsString("wls_scrape_cpu_seconds{instance=\"localhost:7001\"} 3.20"));
    }

    @Test
    public void producedMetricsAreCompliant() {
        performanceProbe.incrementElapsedTime(20);
        performanceProbe.incrementCpuTime(3);

        assertThat(getPrintedMetrics(), followsPrometheusRules());
    }

    @SuppressWarnings("SameParameterValue")
    static class PerformanceProbeStub implements MetricsStream.PerformanceProbe {
        private long currentTime = getRandom();
        private long currentCpu = getRandom();

        private static long getRandom() {
            return ((long) (Math.random() * 100 * NANOSEC_PER_SECONDS));
        }
        void incrementElapsedTime(double seconds) {
            currentTime += seconds * NANOSEC_PER_SECONDS;
        }

        void incrementCpuTime(double seconds) {
            currentCpu += seconds * NANOSEC_PER_SECONDS;
        }

        @Override
        public long getCurrentTime() {
            return currentTime;
        }

        @Override
        public long getCurrentCpu() {
            return currentCpu;
        }
    }
}
