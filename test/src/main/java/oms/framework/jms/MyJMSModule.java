package oms.framework.jms;

import javax.jms.Connection;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

import com.mockrunner.jms.JMSTestModule;
import com.mockrunner.mock.jms.JMSMockObjectFactory;
import com.mockrunner.mock.jms.MockQueueConnection;
import com.mockrunner.mock.jms.MockQueueSession;

/*
 * This Class is a JMS harness class. it is primarily used to connect Sterling OMS to a simple JMS Mock runner
 * this allows the developer to be independent of JMS Implementations. the Developer does not need
 * to install a JMS provider onto his system.
 * In Order to hav Sterling use this Class, you must add this class as JMS queconnection factory. then
 * you can override it using customeroverrides.properties.
 *
 *
 */

public class MyJMSModule extends JMSTestModule implements QueueConnectionFactory {
  public static MyJMSModule getInstance() {
    JMSMockObjectFactory objectFactory = new JMSMockObjectFactory();
    MyJMSModule myJMSModule = new MyJMSModule(objectFactory);
    return myJMSModule;
  }

  private JMSMockObjectFactory myMockFactory;

  public MyJMSModule(JMSMockObjectFactory mockFactory) {
    super(mockFactory);
    myMockFactory = mockFactory;
  }

  @Override
  @SuppressWarnings("serial")
  public MockQueueConnection getCurrentQueueConnection() {
    QueueConnection queueConnection = null;
    try {
      queueConnection = myMockFactory.getMockQueueConnectionFactory().createQueueConnection();

      MockQueueSession queueSession = new MockQueueSession((MockQueueConnection) queueConnection) {
        @Override
        public void rollback() throws JMSException {
          System.out.println("Mock JMS rollback called");
        }

        @Override
        public void setMessageListener(MessageListener listener) {
          try {
            super.setMessageListener(listener);
          } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }

      };

      queueSession.setMessageListener(new MyMessageListener());
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return (MockQueueConnection) queueConnection;
  }

  public Connection createConnection() throws JMSException {
    return null;
  }

  public Connection createConnection(String arg0, String arg1) throws JMSException {
    return null;
  }

  @SuppressWarnings("serial")
  public QueueConnection createQueueConnection() throws JMSException {
    QueueConnection queueConnection = this.getCurrentQueueConnection();
    if (queueConnection == null) {
      queueConnection = myMockFactory.getMockQueueConnectionFactory().createQueueConnection();
    }

    MockQueueSession queueSession = new MockQueueSession((MockQueueConnection) queueConnection) {
      @Override
      public void rollback() throws JMSException {
        System.out.println("Mock JMS rollback called");
      }

      @Override
      public void setMessageListener(MessageListener listener) {
        try {
          super.setMessageListener(listener);
        } catch (JMSException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

    };

    queueSession.setMessageListener(new MyMessageListener());

    return queueConnection;
  }

  public QueueConnection createQueueConnection(String arg0, String arg1) throws JMSException {
    return null;
  }

  public void buildQueue(String queueName) {
    super.getDestinationManager().createQueue(queueName);
  }

  public JMSContext createContext() {
    // TODO Auto-generated method stub
    return null;
  }

  public JMSContext createContext(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public JMSContext createContext(String arg0, String arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public JMSContext createContext(String arg0, String arg1, int arg2) {
    // TODO Auto-generated method stub
    return null;
  }
};
