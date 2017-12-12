package io.prometheus.wls.rest.domain;

/**
 *
 */
public class StringJoiner {

  static String join(String delimeter, String[] list) {
    StringBuilder join = new StringBuilder();
    for (int i = 0; i < list.length; i++) {
      String s = list[i];
      if (i != list.length - 1) {
        s = s + delimeter;
      }
      join.append(s);
    }
    return join.toString();
  }
}
