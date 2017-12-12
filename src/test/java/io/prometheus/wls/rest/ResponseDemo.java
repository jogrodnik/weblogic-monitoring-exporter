package io.prometheus.wls.rest;
/*
 * Copyright (c) 2017 Oracle and/or its affiliates
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Russell Gold
 */
public class ResponseDemo {

    public static void main(String... args) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("deploymentState", 2);
        map3.put("name", "EjbStatusBean");
        map3.put("type", "EJBComponentRuntime");
        //Map<String, Object> map3 = ImmutableMap.of("deploymentState", 2, "name", "EjbStatusBean", "type", "EJBComponentRuntime");
        ItemHolder items2 = new ItemHolder(map3);
        Map<String, Object> map1 = ImmutableMap.of("internal", "false", "name", "mbeans", "componentRuntimes", items2);
        ItemHolder items0 = new ItemHolder(map1);

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(
                ImmutableMap.of("name", "ejb30flexadmin", "applicationRuntimes", items0)));
    }

}
