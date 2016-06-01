package oms.sample.logger;

import oms.framework.testing.ObserverConnector;

public class PrettyLoggerConnector implements ObserverConnector {

  private PrettyLogger logger;

  public PrettyLoggerConnector(String string) {
    logger = new PrettyLogger(string);
  }

  public PrettyLoggerConnector() {
    logger = new PrettyLogger();
  }

  public void send(Object theObject) {
    logger.log(theObject);
  }

  public void open() {
    logger.log(new String(new char[120]).replace("\0", PrettyLogger.DEFAULT_PRETTYFORMAT));
  }

  public void close() {
    logger.log(new String(new char[120]).replace("\0", PrettyLogger.DEFAULT_PRETTYFORMAT));
  }
}