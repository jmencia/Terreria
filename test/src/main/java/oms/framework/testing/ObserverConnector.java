package oms.framework.testing;

//these classes allow you to connect what the observer is observing to an output
//examples of outputs:
//Log4j
//Slfj
//System.out
//FileWriter

public interface ObserverConnector {

  public void send(Object theObject);

  public void open();

  public void close();

}
