package oms.framework.utils;

import java.io.File;
import java.io.FileReader;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public abstract class PropertiesReader implements ResourceReader {
  private String fileName;

  protected PropertiesReader(String fileName) {
    this.fileName = fileName;
  }

  public Set<String> read() {
    Set<String> resources = new TreeSet<String>();
    Properties props = new Properties();
    try {
      props.load(new FileReader(new File(fileName)));
    } catch (Exception exc) {
      System.out.println(exc);
    }

    for (Entry<Object, Object> entry : props.entrySet()) {
      if (interestedInEntry((String) entry.getKey(), (String) entry.getValue())) {
        resources.add((String) entry.getValue());
      }
    }
    return resources;
  }

  protected abstract boolean interestedInEntry(String key, String value);

}
