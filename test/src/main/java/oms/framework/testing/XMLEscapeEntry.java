package oms.framework.testing;

import java.util.Map;

public class XMLEscapeEntry implements Map.Entry<String, String>, Comparable<XMLEscapeEntry> {

  String key;
  String value;

  public XMLEscapeEntry(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public String setValue(String value) {
    this.value = value;
    return value;
  }

  public int compareTo(XMLEscapeEntry comparedEntry) {

    if (key.equals("&amp;")) {
      return -1;
    } else {
      return this.equals(comparedEntry) ? 0 : 1;
    }

  }

}
