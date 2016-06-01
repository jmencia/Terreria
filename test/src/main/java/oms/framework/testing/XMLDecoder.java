package oms.framework.testing;

import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

public class XMLDecoder {

  private static SortedSet<Entry<String, String>> xmlEscapeSet = new TreeSet<Entry<String, String>>();

  static {
    xmlEscapeSet.add(new XMLEscapeEntry("&amp;", "&"));
    xmlEscapeSet.add(new XMLEscapeEntry("&lt;", "<"));
    xmlEscapeSet.add(new XMLEscapeEntry("&gt;", ">"));
  }

  public static String decode(String input) {
    if (input != null) {
      for (Entry<String, String> entry : xmlEscapeSet) {
        System.out.println(entry.getKey());
        input = input.replaceAll(entry.getKey(), entry.getValue());
      }
    }
    return input;
  }

}