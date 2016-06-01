package oms.framework.jms;

import javax.jms.Message;
import javax.jms.MessageListener;

public class MyMessageListener implements MessageListener {

  public void onMessage(Message message) {
    System.out.println("this is the message received");
    System.out.println(message);

  }

}
