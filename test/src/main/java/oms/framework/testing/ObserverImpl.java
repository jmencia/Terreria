package oms.framework.testing;

public class ObserverImpl implements Observer {
  final ObserverConnector connector;

  public ObserverImpl(ObserverConnector connector) {
    this.connector = connector;
  }

  public ObserverImpl() {
    connector = null;
    // need to create a basic connecter that just writes to System.out;
  }

  public void observe(Object theObject) {
    if (connector != null) {
      connector.send(theObject);
    } else {
      System.out.println(theObject.toString());
    }
  }

  public void startObservation() {
    if (connector != null) {
      connector.open();
    }
  }

  public void endObservation() {
    if (connector != null) {
      connector.close();
    }
  }

  public void explainObservation() {
    if (connector != null) {
      connector.close();
    }
  }

}

// public void changeOutputTo(String outputTo)
// {
// this.outputType = outputTo;

// }

// public void revertOutputBack()
// {
// if (this.originaloutPutType != null)
// {
// this.outputType = originaloutPutType;
// this.originaloutPutType = null;
// }

// }

// public static Writer getFile(String fileName) throws Exception
// {
// File f = new File(fileName);
// f.createNewFile();

// File file = new File(fileName);

// if (!file.exists())
// {
// file.createNewFile();
// }

// return new FileWriter(file, true);
// }
