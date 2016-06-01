package oms.framework.exception;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;

/*
 * This class is the Utility class to register Exception handles. by default
 * if it receives a message it will try to call interested Exception handlers for it.
 * if there are none then it returns the exception. Also if any handlers fail it will not cause
 * any workflows to fail.
 *
 * need to add the ability to deregister an ExceptionHandler. also if an eceptionhandler fails consistently
 * the ability to auto-deregister it.
 */
public class ExceptionUtils {

  // TODO create exception model
  public static Set<ExceptionHandler> handlers = new TreeSet<ExceptionHandler>();

  public static String getReadableMessage(Throwable exc) {
    System.out.println("Size of Exception handlers =" + handlers.size());
    for (ExceptionHandler handler : handlers) {
      if (handler.interestedInException().isAssignableFrom(exc.getClass())) {
        try {
          String message = handler.HandleException(exc);
          return message;
        } catch (Throwable e) {
          StringBuilder msg = new StringBuilder("The handler ").append(handler.getClass().getCanonicalName())
              .append(
                  " is not configured properly. please verify the handler is configured properly within your Environment")
              .append("\n. The handlers error will not affect or stop the workflow of the System");
          System.out.println(msg);
        }

      }
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);

    exc.printStackTrace(ps);
    return "<raw-message>" + baos.toString() + "</raw-message>";
  }

  public static void setHandler(ExceptionHandler handler) {
    handlers.add(handler);
  }

  public static void removeHandler(ExceptionHandler handler) {
    handlers.remove(handler);
  }

  public static void clearAllHandlers() {
    handlers.clear();
  }
}
