package io.prometheus.wls.rest;
/*
 * Copyright (c) 2017 Oracle and/or its affiliates
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

import io.prometheus.wls.rest.domain.MBeanSelector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static io.prometheus.wls.rest.domain.MapUtils.isNullOrEmptyString;

/**
 * The servlet which produces the exported metrics.
 *
 * @author Russell Gold
 */
@WebServlet(value = "/" + ServletConstants.METRICS_PAGE)
public class ExporterServlet extends PassThroughAuthenticationServlet {

    @SuppressWarnings("unused")  // production constructor
    public ExporterServlet() {
        this(new WebClientFactoryImpl());
    }

    ExporterServlet(WebClientFactory webClientFactory) {
        super(webClientFactory);
    }

    @Override
    public void init(ServletConfig servletConfig) {
        LiveConfiguration.init(servletConfig);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AuthenticatedService authenticatedService = new AuthenticatedService() {
            @Override
            public void execute(WebClient webClient, HttpServletRequest req, HttpServletResponse resp) throws IOException {
                displayMetrics(webClient, req, resp);
            }
        };
        doWithAuthentication(req, resp, authenticatedService);
    }

    @SuppressWarnings("unused") // The req parameter is not used, but is required by 'doWithAuthentication'
    private void displayMetrics(WebClient webClient, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LiveConfiguration.updateConfiguration();
        try (MetricsStream metricsStream = new MetricsStream(resp.getOutputStream())) {
            if (!LiveConfiguration.hasQueries())
                metricsStream.println("# No configuration defined.");
            else
                printMetrics(webClient, metricsStream);

        }
    }

    private void printMetrics(WebClient webClient, MetricsStream metricsStream) throws IOException {
        for (MBeanSelector selector : LiveConfiguration.getQueries())
            displayMetrics(webClient, metricsStream, selector);
        metricsStream.printPerformanceMetrics();
    }

    private void displayMetrics(WebClient webClient, MetricsStream metricsStream, MBeanSelector selector) throws IOException {
        try {
            Map<String, Object> metrics = getMetrics(webClient, selector);
            if (metrics != null) {
                Set<Map.Entry<String, Object>> metricSet = sort(metrics).entrySet();
                for(Map.Entry<String, Object> metric : metricSet) {
                    metricsStream.printMetric(metric.getKey(), metric.getValue());
                }
            }
                //sort(metrics).forEach(metricsStream::printMetric);
        } catch (RestQueryException e) {
            metricsStream.println("REST service was unable to handle this query\n" + selector.getPrintableRequest());
        }
    }

    private TreeMap<String, Object> sort(Map<String, Object> metrics) {
        return new TreeMap<>(metrics);
    }

    private Map<String, Object> getMetrics(WebClient webClient, MBeanSelector selector) throws IOException {
        String jsonResponse = webClient.doPostRequest(selector.getRequest());
        if (isNullOrEmptyString(jsonResponse)) return null;

        return LiveConfiguration.scrapeMetrics(selector, jsonResponse);
    }

}
