package oms.framework.utils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;

public class PollutionReducerPrintStream extends PrintStream {
  private Set<String> pollutionSet = new TreeSet<String>();

  public PollutionReducerPrintStream(OutputStream arg0) {
    super(arg0);
  }

  @Override
  public void println(String s) {
    for (String pollution : pollutionSet) {
      if (s.contains(pollution)) {
        return;
      }
    }
    super.println(s);
  }

  @Override
  public void print(String s) {
    for (String pollution : pollutionSet) {
      if (s.contains(pollution)) {
        return;
      }
    }
    super.print(s);
  }

  public void clearPollutionSet() {
    pollutionSet.clear();
  }

  public void setPollution(String pollution) {
    pollutionSet.add(pollution);
  }
}
