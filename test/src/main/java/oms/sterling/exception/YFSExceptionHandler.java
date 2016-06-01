package oms.sterling.exception;

import com.yantra.yfs.japi.YFSException;

import oms.framework.exception.AbstractExceptionHandler;
import oms.framework.testing.XMLDecoder;

public class YFSExceptionHandler extends AbstractExceptionHandler {

  public YFSExceptionHandler(Long priority, Class<YFSException> clazz) {
    super(priority, clazz);

  }

  public String HandleException(Throwable exc) {
    System.out.println("I am handlling this exception" + this.getClass().getName());

    String exceptionMessage = exc.getMessage();
    exceptionMessage = XMLDecoder.decode(exceptionMessage);
    if (exceptionMessage.contains("java.lang.reflect.InvocationTargetException")) {
      System.out.println("Exception contained InvocationTarget Exception removing wrapper");
      exceptionMessage = removeWrapperException(exceptionMessage);
    } else {
      System.out.println("Exception did not contained InvocationTarget.");
    }

    StringBuilder message = new StringBuilder();
    message.append(exceptionMessage);

    // this.doExtraStuff(message.toString());
    return message.toString();
  }

  @SuppressWarnings("unchecked")
  public Class<YFSException> interestedInException() {
    return YFSException.class;
  }

  private String removeWrapperException(String message) {
    message = message.substring(message.lastIndexOf("<Errors"));
    message = message.substring(0, message.indexOf("</Error>"));
    message = message.concat("</Error>\n</Error>\n</Errors>");

    message += ("\n\n<!--this message was intercepted-->");
    return message;

  }

  // private void doExtraStuff(String message)
  // {
  // try
  // {
  // DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  // DocumentBuilder documentBuilder = factory.newDocumentBuilder();
  // documentBuilder.parse(new ByteArrayInputStream(message.getBytes()));
  // } catch (Exception e)
  // {
  // System.out.println(e.getMessage());
  // throw new NullPointerException();
  // }
  // }
}
