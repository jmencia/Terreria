package oms.sample.logger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import oms.framework.transformer.DocumentToStringTransformer;

public class PrettyLogger implements Logger {
  public static final CharSequence DEFAULT_PRETTYFORMAT = "*";
  private String prettyFormat = "*";
  private Boolean headerPrinted = false;
  private static DocumentToStringTransformer transformer = new DocumentToStringTransformer();
  private String outputType = "nil";
  private Writer writer;
  private String originaloutPutType = null;

  public PrettyLogger(String outputType) {
    this.outputType = outputType;
    writer = this.initiateWriter();
  }

  public PrettyLogger() {
    outputType = "console://";
    writer = this.initiateWriter();
  }

  public void changeOutputTo(String outputTo) {
    outputType = outputTo;

  }

  public void revertOutputBack() {
    if (originaloutPutType != null) {
      outputType = originaloutPutType;
      originaloutPutType = null;
    }

  }

  public void log(Object theObject) {
    if (theObject == null) {
      logMessage("The Message was not writeable it contained null information");
      return;
    } else if (theObject instanceof String) {
      this.logMessage((String) theObject);
    } else if (theObject instanceof Document) {
      this.logDocument((Document) theObject);
    } else if (theObject instanceof String[]) {
      this.logMessages((String[]) theObject, false);
    } else {
      this.logMessage(theObject.toString(), false);
    }

  }

  public void logStartCycle() {
    this.logMessage(new String(new char[120]).replace("\0", prettyFormat), false);
  }

  public void logEndCycle() {
    this.logMessage(new String(new char[120]).replace("\0", prettyFormat), false);
    headerPrinted = false;
  }

  public void finishLogging() {
    try {
      writer.flush();

    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void logMessage(String message) {
    logMessage(message, true);
  }

  private void logMessage(String message, boolean applyFormat) {
    List<String> formattedMessages = new ArrayList<String>();
    if (applyFormat) {
      formattedMessages = getFormattedMessage(message);
    } else {
      formattedMessages.add(message);
    }
    for (String formattedMessage : formattedMessages) {
      try {
        writer.write(formattedMessage + "\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void logDocument(Document document) {
    if (document == null) {
      logMessage("Document was empty:nothing to write");
    }
    String[] messages = transformer.transform(document).split("\n");

    logMessages(messages, true);
  }

  private void logMessages(String[] messages, Boolean wasDocument) {

    for (String message : messages) {
      if (wasDocument) {
        String rawDocumentMessages[] = message.split("\" ");

        String[] documentMessages = new String[rawDocumentMessages.length];
        int x = 0;
        for (String documentMessage : rawDocumentMessages) {
          documentMessages[x] = documentMessage + (documentMessage.endsWith(">") ? "" : "\"");
          x++;
        }
        this.logMessages(documentMessages, false);

      } else {
        this.logMessage(message, true);
      }
    }
  }

  private Writer initiateWriter() {
    Writer writer = new Writer() {
      @Override
      public void close() throws IOException {
      }

      @Override
      public void flush() throws IOException {
      }

      @Override
      public void write(char[] arg0, int arg1, int arg2) throws IOException {
      }
    };

    if (outputType.contains("console://")) {
      return new OutputStreamWriter(System.out);
    }
    return writer;

  }

  private List<String> getFormattedMessage(String message) {
    List<String> formattedMessages = new ArrayList<String>();

    do {
      String formattedMessage = message.length() > 120 ? message.subSequence(0, 120).toString() : message;

      if (!headerPrinted) {
        formattedMessage = String.format("%1$-112s", "\t" + formattedMessage);
        formattedMessage = String.format("%1$s%2$s%1$s", "*", formattedMessage);
        formattedMessages.add(formattedMessage);
        headerPrinted = true;
      } else {
        formattedMessage = String.format("%1$-105s", "\t\t" + formattedMessage);
        formattedMessage = String.format("%1$s%2$s%1$s", "*", formattedMessage);
        formattedMessages.add(formattedMessage);

      }

    } while ((message.length() > 120) && ((message = message.substring(120)).length()) > 0);
    return formattedMessages;
  }
}

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
