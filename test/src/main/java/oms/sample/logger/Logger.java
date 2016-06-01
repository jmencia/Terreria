package oms.sample.logger;

public interface Logger {
  public void log(Object theObject);

  public void logStartCycle();

  public void logEndCycle();

  public void finishLogging();

  public void changeOutputTo(String outputTo);

  public void revertOutputBack();

}
