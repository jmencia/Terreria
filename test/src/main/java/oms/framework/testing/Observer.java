package oms.framework.testing;

public interface Observer {
  public void observe(Object theObject);

  public void startObservation();

  public void endObservation();

  public void explainObservation();
  // public void changeOutputTo(String outputTo);

  // public void revertOutputBack();

}
