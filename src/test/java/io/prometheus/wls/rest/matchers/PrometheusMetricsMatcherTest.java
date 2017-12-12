package io.prometheus.wls.rest.matchers;
/*
 * Copyright (c) 2017 Oracle and/or its affiliates
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
import org.junit.Test;

import static io.prometheus.wls.rest.matchers.PrometheusMetricsMatcher.followsPrometheusRules;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Russell Gold
 */
public class PrometheusMetricsMatcherTest {

    private static final String delimeter = "\n";

    private final PrometheusMetricsMatcher matcher = followsPrometheusRules();

    @Test
    public void whenMetricsAreGrouped_matcherPasses() {
        assertTrue(matcher.matches(toHtml(ORDERED_LIST)));
    }

    private final String[] ORDERED_LIST =
            {"metric1 1", "metric2{name='red'} 23", "metric2{name='blue'} 34"};

    private String toHtml(String... metrics) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < metrics.length; i++) {
            String s = metrics[i];
            String s1 = s.replace('\'', '"');
            if (i != metrics.length -1) {
                s1 = s1 + delimeter;
            }
            result.append(s1);
        }
        //return Arrays.stream(metrics).map((s) -> s.replace('\'', '"')).collect(joining("\n"));
        return result.toString();
    }

    @Test
    public void whenMetricsAreInterspersed_matcherFails() {
        assertFalse(matcher.matches(toHtml(MISORDERED_LIST)));
    }

    private final String[] MISORDERED_LIST =
            {"metric2{name='red'} 23", "metric1 1", "metric2{name='blue'} 34"};

    @Test
    public void whenMetricsHaveNonNumericValues_matcherFails() {
        final String toHtml = toHtml(TEXT_LIST);
        final boolean matches = matcher.matches(toHtml);
        assertFalse(matches);
    }

    private final String[] TEXT_LIST =
            {"metric1 1", "metric2{name='red'} 23", "metric2{name='blue'} some-color"};
}
