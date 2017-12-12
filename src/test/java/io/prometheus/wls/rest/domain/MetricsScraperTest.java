package io.prometheus.wls.rest.domain;
/*
 * Copyright (c) 2017 Oracle and/or its affiliates
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hamcrest.Description;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.prometheus.wls.rest.domain.MetricsScraperTest.MetricMatcher.hasMetric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Russell Gold
 */
public class MetricsScraperTest {
    private static final String RESPONSE =
            "{\"applicationRuntimes\": {\"items\": [\n" +
            "     {\n" +
            "            \"internal\": false,\n" +
            "            \"name\": \"mbeans\",\n" +
            "            \"componentRuntimes\": {\"items\": [{\n" +
            "                \"deploymentState\": 2,\n" +
            "                \"name\": \"EjbStatusBean\",\n" +
            "                \"type\": \"EJBComponentRuntime\"\n" +
            "              }]}\n" +
            "     },\n" +
            "     {\n" +
            "            \"internal\": true,\n" +
            "            \"name\": \"weblogic\",\n" +
            "            \"componentRuntimes\": {\"items\": [{\n" +
            "                \"type\": \"WebAppComponentRuntime\",\n" +
            "                \"sourceInfo\": \"weblogic.war\",\n" +
            "                \"contextRoot\": \"\\/weblogic\",\n" +
            "                \"openSessionsCurrentCount\": 0,\n" +
            "                \"deploymentState\": 2,\n" +
            "                \"sessionsOpenedTotalCount\": 0,\n" +
            "                \"name\": \"ejb30_weblogic\",\n" +
            "                \"openSessionsHighCount\": 0,\n" +
            "                \"servlets\": {\"items\": [\n" +
            "                    {\n" +
            "                        \"servletName\": \"JspServlet\",\n" +
            "                        \"invocationTotalCount\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"servletName\": \"FileServlet\",\n" +
            "                        \"invocationTotalCount\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"servletName\": \"ready\",\n" +
            "                        \"invocationTotalCount\": 2\n" +
            "                    }\n" +
            "                  ]}\n" +
            "            }]}\n" +
            "     }\n" +
            "]}}";

    private static final String TWO_LEVEL_RESPONSE ="{\n" +
            "            \"internal\": true,\n" +
            "            \"name\": \"weblogic\",\n" +
            "            \"componentRuntimes\": {\"items\": [{\n" +
            "                \"type\": \"WebAppComponentRuntime\",\n" +
            "                \"sourceInfo\": \"weblogic.war\",\n" +
            "                \"internal\": true,\n" +
            "                \"openSessionsCurrentCount\": 0,\n" +
            "                \"deploymentState\": 2,\n" +
            "                \"sessionsOpenedTotalCount\": 0,\n" +
            "                \"name\": \"ejb30_weblogic\",\n" +
            "                \"openSessionsHighCount\": 0,\n" +
            "                \"servlets\": {\"items\": [\n" +
            "                    {\n" +
            "                        \"servletName\": \"JspServlet\",\n" +
            "                        \"invocationTotalCount\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"servletName\": \"FileServlet\",\n" +
            "                        \"invocationTotalCount\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"servletName\": \"ready\",\n" +
            "                        \"invocationTotalCount\": 2\n" +
            "                    }\n" +
            "                ]}\n" +
            "            }]}\n" +
            "        }";

    private static final String SERVLET_RESPONSE = "{\n" +
            "                \"servlets\": {\"items\": [\n" +
            "                    {\n" +
            "                        \"servletName\": \"JspServlet\",\n" +
            "                        \"invocationHighCount\": 10,\n" +
            "                        \"invocationTotalCount\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"servletName\": \"FileServlet\",\n" +
            "                        \"invocationHighCount\": 15,\n" +
            "                        \"invocationTotalCount\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"servletName\": \"ready\",\n" +
            "                        \"invocationHighCount\": 30,\n" +
            "                        \"invocationTotalCount\": 2\n" +
            "                    }\n" +
            "                ]}\n" +
            "            }";

    private static final String SINGLE_SERVLET_RESPONSE = "{\n" +
            "                \"servlets\": {\"items\": [\n" +
            "                    {\n" +
            "                        \"servletName\": \"JspServlet\",\n" +
            "                        \"invocationId\": 23,\n" +
            "                        \"invocationTotalCount\": 0\n" +
            "                    }\n" +
            "                ]}\n" +
            "            }";

    private final Map<String,Object> leafMap = new HashMap<>();
    {
        Map<String, Object> map = new HashMap<>();
        map.put(MBeanSelector.KEY, "servletName");
        map.put(MBeanSelector.PREFIX, "servlet_");
        map.put(MBeanSelector.VALUES, new String[]{"invocationTotalCount","invocationId"});
//        ImmutableMap.of(MBeanSelector.KEY, "servletName",
//            MBeanSelector.PREFIX, "servlet_", MBeanSelector.VALUES, new String[]{"invocationTotalCount","invocationId"})
        leafMap.putAll(map);
    }

    private final Map<String,Object> emptyLeafMap = new HashMap<>();
    {
        Map<String, Object> map = new HashMap<>();
//        ImmutableMap.of(MBeanSelector.KEY, "servletName",
//            MBeanSelector.PREFIX, "servlet_")
        map.put(MBeanSelector.KEY, "servletName");
        map.put(MBeanSelector.PREFIX, "servlet_");
        emptyLeafMap.putAll(map);
    }

    private final Map<String,Object> componentMap = new HashMap<>(
            ImmutableMap.of(MBeanSelector.KEY_NAME, "component", MBeanSelector.KEY, "name", MBeanSelector.PREFIX, "component_",
                            "servlets", leafMap,
                            MBeanSelector.VALUES, new String[]{"sourceInfo", "internal", "deploymentState"}));

    private final Map<String,Object> noPrefixComponentMap = new HashMap<>(
            ImmutableMap.of(MBeanSelector.KEY_NAME, "component", MBeanSelector.KEY, "name",
                            "servlets", leafMap,
                            MBeanSelector.VALUES, new String[]{"sourceInfo", "internal", "deploymentState"}));

//    private final Map<String,Object> twoLevelMap = ImmutableMap.of("componentRuntimes", componentMap);
    private final Map<String,Object> twoLevelMap = new HashMap<>();//ImmutableMap.of("componentRuntimes", componentMap);
    {
        twoLevelMap.put("componentRuntimes", componentMap);
    }

//    private final Map<String,Object> noPrefixTwoLevelMap = ImmutableMap.of("componentRuntimes", noPrefixComponentMap);
    private final Map<String,Object> noPrefixTwoLevelMap = new HashMap<>();
    {
        noPrefixTwoLevelMap.put("componentRuntimes", noPrefixComponentMap);
    }

    private final MetricsScraper scraper = new MetricsScraper();

    @Test
    public void generateLeafMetrics() {
        generateNestedMetrics(getServletsMap(), SERVLET_RESPONSE);

        assertThat(scraper.getMetrics(),
                   allOf(hasMetric("servlet_invocationTotalCount{servletName=\"JspServlet\"}", 0),
                         hasMetric("servlet_invocationTotalCount{servletName=\"FileServlet\"}", 1),
                         hasMetric("servlet_invocationTotalCount{servletName=\"ready\"}", 2)));
    }

    private Map<String, Object> getServletsMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("servlets", leafMap);
//        return ImmutableMap.of("servlets", leafMap);
        return map;
    }

    private void generateNestedMetrics(Map<String,Object> map, String jsonString) {
        generateNestedMetrics(map, jsonString, "");
    }

    private void generateNestedMetrics(Map<String,Object> map, String jsonString, String parentQualifiers) {
        scraper.scrapeSubObject(getJsonResponse(jsonString), MBeanSelector.create(map), parentQualifiers);
    }

    private JsonObject getJsonResponse(String jsonString) {
        return new JsonParser().parse(jsonString).getAsJsonObject();
    }

    @Test
    public void whenNoValuesSpecified_generateAllMetrics() {
        generateNestedMetrics(getAllValuesServletsMap(), SERVLET_RESPONSE);

        assertThat(scraper.getMetrics(),
                   allOf(hasMetric("servlet_invocationHighCount{servletName=\"JspServlet\"}", 10),
                         hasMetric("servlet_invocationHighCount{servletName=\"FileServlet\"}", 15),
                         hasMetric("servlet_invocationTotalCount{servletName=\"ready\"}", 2)));
    }

    private Map<String, Object> getAllValuesServletsMap() {
        //return ImmutableMap.of("servlets", emptyLeafMap);
        Map<String, Object> map = new HashMap<>();
        map.put("servlets", emptyLeafMap);
        return map;
    }

    @Test
    public void generateLeafMetricsWhileAccumulatingQualifiers() {
        generateNestedMetrics(getServletsMap(), SERVLET_RESPONSE, "webapp=\"wls\"");

        assertThat(scraper.getMetrics(),
                   hasMetric("servlet_invocationTotalCount{webapp=\"wls\",servletName=\"JspServlet\"}", 0));
    }

    @Test
    public void generateLeafMetricsWithNoQualifiers() {
        generateNestedMetrics(getServletsMapWithoutQualifierKey(), SINGLE_SERVLET_RESPONSE);

        assertThat(scraper.getMetrics(), hasMetric("servlet_invocationTotalCount", 0));
    }

    private Map<String, Object> getServletsMapWithoutQualifierKey() {
        leafMap.remove(MBeanSelector.KEY);
//        return ImmutableMap.of("servlets", leafMap);
        Map<String, Object> map = new HashMap<>();
        map.put("servlets", leafMap);
        return map;
    }

    @Test
    public void generateLeafMetricsWithParentQualifiersOnly() {
        generateNestedMetrics(getServletsMapWithoutQualifierKey(), SINGLE_SERVLET_RESPONSE, "webapp=\"wls\"");

        assertThat(scraper.getMetrics(), hasMetric("servlet_invocationTotalCount{webapp=\"wls\"}", 0));
    }

    @Test
    public void whenKeyNameSpecified_useItRatherThanKey() {
        generateNestedMetrics(getServletsMapWithKeyName("servlet"), SERVLET_RESPONSE);

        assertThat(scraper.getMetrics(), hasMetric("servlet_invocationTotalCount{servlet=\"JspServlet\"}", 0));
    }

    @SuppressWarnings("SameParameterValue")
    private Map<String, Object> getServletsMapWithKeyName(String keyName) {
        leafMap.put(MBeanSelector.KEY_NAME, keyName);
//        return ImmutableMap.of("servlets", leafMap);
        Map<String, Object> map = new HashMap<>();
        map.put("servlets", leafMap);
        return map;
    }

    @Test
    public void whenValuesIncludesKey_ignoreIt() {
        generateNestedMetrics(getServletsMapWithKey("invocationId"), SINGLE_SERVLET_RESPONSE);

        assertThat(scraper.getMetrics(), not(hasMetric("servlet_invocationId{invocationId=\"23\"}", 23)));
    }

    @SuppressWarnings("SameParameterValue")
    private Map<String, Object> getServletsMapWithKey(String key) {
        leafMap.put(MBeanSelector.KEY, key);
//        return ImmutableMap.of("servlets", leafMap);
        Map<String, Object> map = new HashMap<>();
        map.put("servlets", leafMap);
        return map;
    }

    @Test
    public void whenGenerateHierarchicalMetrics_containsTopLevel() {
        generateNestedMetrics(twoLevelMap, TWO_LEVEL_RESPONSE);

        assertThat(scraper.getMetrics(), hasMetric("component_deploymentState{component=\"ejb30_weblogic\"}", 2));
    }

    @Test
    public void whenNoPrefix_generateBareMetrics() {
        generateNestedMetrics(noPrefixTwoLevelMap, TWO_LEVEL_RESPONSE);

        assertThat(scraper.getMetrics(), hasMetric("deploymentState{component=\"ejb30_weblogic\"}", 2));
    }

    @Test
    public void whenGenerateHierarchicalMetrics_ignoresNonNumericValues() {
        generateNestedMetrics(twoLevelMap, TWO_LEVEL_RESPONSE);

        assertThat(scraper.getMetrics(), not(hasMetric("component_sourceInfo{component=\"ejb30_weblogic\"}", "weblogic.war")));
        assertThat(scraper.getMetrics(), not(hasMetric("component_internal{component=\"ejb30_weblogic\"}", "true")));
    }

    @Test
    public void whenGenerateHierarchicalMetrics_containsBottomLevel() {
        generateNestedMetrics(twoLevelMap, TWO_LEVEL_RESPONSE);

        assertThat(scraper.getMetrics(), hasMetric("servlet_invocationTotalCount{component=\"ejb30_weblogic\",servletName=\"JspServlet\"}", 0));
    }

    @Test
    public void whenResponseLacksServerRuntimes_generateEmptyMetrics() {
        assertThat(scraper.scrape(MBeanSelector.create(getFullMap()), getJsonResponse("{}")), anEmptyMap());
    }

    @Test
    public void generateFromFullResponse() {
        Map<String, Object> metrics = scraper.scrape(MBeanSelector.create(getFullMap()), getJsonResponse(RESPONSE));
        
        assertThat(metrics, hasMetric("component_deploymentState{application=\"weblogic\",component=\"ejb30_weblogic\"}", 2));
        assertThat(metrics, hasMetric("servlet_invocationTotalCount{application=\"weblogic\",component=\"ejb30_weblogic\",servletName=\"JspServlet\"}", 0));
    }

    private Map<String, Object> getFullMap() {
//        return ImmutableMap.of("applicationRuntimes",
//                    ImmutableMap.of(MBeanSelector.KEY_NAME, "application", MBeanSelector.KEY, "name",
//                                    "componentRuntimes", componentMap));
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapLeaf = new HashMap<>();
        mapLeaf.put(MBeanSelector.KEY_NAME, "application");
        mapLeaf.put(MBeanSelector.KEY, "name");
        mapLeaf.put("componentRuntimes", componentMap);
        map.put("applicationRuntimes", mapLeaf);
        return map;
    }


    @Test
    public void generateFromFullResponseUsingSnakeCase() {
        scraper.setMetricNameSnakeCase(true);
        Map<String, Object> metrics = scraper.scrape(MBeanSelector.create(getFullMap()), getJsonResponse(RESPONSE));

        assertThat(metrics, hasMetric("component_deployment_state{application=\"weblogic\",component=\"ejb30_weblogic\"}", 2));
        assertThat(metrics, hasMetric("servlet_invocation_total_count{application=\"weblogic\",component=\"ejb30_weblogic\",servletName=\"JspServlet\"}", 0));
    }

    @Test
    public void whenTypeNotSpecified_includeAllComponents() {
        Map<String, Object> metrics = scraper.scrape(MBeanSelector.create(getFullMap()), getJsonResponse(RESPONSE));

        assertThat(metrics, hasMetric("component_deploymentState{application=\"weblogic\",component=\"ejb30_weblogic\"}", 2));
        assertThat(metrics, hasMetric("component_deploymentState{application=\"mbeans\",component=\"EjbStatusBean\"}", 2));
    }

    @Test
    public void selectOnlyWebApps() {
        componentMap.put(MBeanSelector.TYPE, "WebAppComponentRuntime");
        Map<String, Object> metrics = scraper.scrape(MBeanSelector.create(getFullMap()), getJsonResponse(RESPONSE));

        assertThat(metrics, hasMetric("component_deploymentState{application=\"weblogic\",component=\"ejb30_weblogic\"}", 2));
        assertThat(metrics, not(hasMetric("component_deploymentState{application=\"mbeans\",component=\"EjbStatusBean\"}", 2)));
    }

    @Test
    public void whenValuesAtTopLevel_scrapeThem() {
        final MBeanSelector selector = MBeanSelector.create(getMetricsMap());
        final JsonObject jsonResponse = getJsonResponse(METRICS_RESPONSE);
        Map<String, Object> metrics = scraper.scrape(selector, jsonResponse);

        assertThat(metrics, hasMetric("heapSizeCurrent", 123456));
    }


    private Map<String, Object> getMetricsMap() {
//        return ImmutableMap.of("JVMRuntime",
//                    ImmutableMap.of(MBeanSelector.KEY, "name", MBeanSelector.VALUES, new String[]{"processCpuLoad", "heapSizeCurrent"}));
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapLeaf = new HashMap<>();
        mapLeaf.put(MBeanSelector.KEY, "name");
        mapLeaf.put(MBeanSelector.VALUES, new String[]{"processCpuLoad", "heapSizeCurrent"});
        map.put("JVMRuntime", mapLeaf);
        return map;
    }


    private static final String METRICS_RESPONSE =
            "{\"JVMRuntime\": {\n" +
            "    \"uptime\": 137762378,\n" +
            "    \"heapSizeCurrent\": 123456,\n" +
            "    \"heapFreeCurrent\": 287379152,\n" +
            "    \"heapSizeMax\": 477626368,\n" +
            "    \"heapFreePercent\": 69,\n" +
            "    \"processCpuLoad\": .0028\n" +
            "}}";

    static class MetricMatcher extends org.hamcrest.TypeSafeDiagnosingMatcher<Map<String,Object>> {
        private String expectedKey;
        private Object expectedValue;

        private MetricMatcher(String expectedKey, Object expectedValue) {
            this.expectedKey = expectedKey;
            this.expectedValue = expectedValue;
        }

        static MetricMatcher hasMetric(String expectedKey, Object expectedValue) {
            return new MetricMatcher(expectedKey, expectedValue);
        }

        @Override
        protected boolean matchesSafely(Map<String, Object> map, Description description) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (expectedKey.equals(entry.getKey()) && matchesValue(entry.getValue())) {
                    return true;
                }
            }
            
            description.appendText("map was ").appendValueList("[", ", ", "]", map.entrySet());
            return false;
        }

        private boolean matchesValue(Object o) {
            if (expectedValue instanceof String)
                return Objects.equals(o, expectedValue);
            else
                return expectedValue instanceof Integer && ((Number) o).intValue() == ((Number) expectedValue).intValue();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("map containing [")
                       .appendValue(expectedKey)
                       .appendText("->")
                       .appendValue(expectedValue)
                       .appendText("]");
        }
    }
}
