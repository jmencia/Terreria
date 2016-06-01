package oms.framework.testing;

import java.io.InputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockejb.MockContainer;
import org.mockejb.jndi.MockContextFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.yantra.interop.client.YIFClientFactoryImpl;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.util.YFSContextManager;
import com.yantra.omp.agent.YFSReleaseOrderAgent;
import com.yantra.shared.dbclasses.YFS_Shipment_ContainerDBHome;
import com.yantra.shared.dbclasses.YFS_Task_QueueDBHome;
import com.yantra.shared.dbi.YFS_Shipment_Container;
import com.yantra.shared.dbi.YFS_Task_Queue;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.yfc.dblayer.YFCDBContext;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import oms.framework.exception.ExceptionHandler;
import oms.framework.exception.ExceptionUtils;
import oms.framework.jms.MyJMSModule;
import oms.framework.jms.MyMessageListener;
import oms.framework.utils.NanoStopWatch;
import oms.framework.utils.PollutionReducerPrintStream;
import oms.framework.utils.PropertiesReader;
import oms.sample.logger.PrettyLoggerConnector;
import oms.sterling.exception.YFSExceptionHandler;

/*
 * Test Client for Java. this is version 0.1 - need to refactor and extrapolate by domain
 * You can use this class but it is still in raw form. next release will start refactoring out to
 * be able to run tests as needed.
 */

@RunWith(OrderedTestRunner.class)
public class SterlingClientTester {
  private static DocumentBuilderFactory factory;
  private static DocumentBuilder documentBuilder;

  private static YIFApi client;
  private YFSEnvironment environment;
  private static String orderNumber = null;

  public static MockContainer mockContainer;
  private static Context context;
  private static Observer observer = new ObserverImpl(new PrettyLoggerConnector("console://"));

  @Rule
  public ExceptionWatcher exceptionWatcher = new ExceptionWatcher();

  @Rule
  public NanoStopWatch stopwatch = new NanoStopWatch(observer);

  @SuppressWarnings("serial")
  @Rule
  public ActorSet<String> jndiNames = new ActorSet<String>(new ActorsForJNDIHandler(new PropertiesReader(
      "/Users/jose/Desktop/repository/jars/sterling/foundation/8.5/bootstrapper/resources/customer_overrides.properties") {
    @Override
    protected boolean interestedInEntry(String key, String value) {
      return (value.contains("jms-queue") || value.contains("jms-topic"));
    }
  })) {
    @Override
    protected void before(String methodName) {
      super.before(methodName);
      try {
        buildAndBind();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

  @BeforeClass
  public static void oneTimeSetUp() throws Exception {
    observer.startObservation();
    observer.observe("Starting setting up the Harness for testing");

    Long startTime = System.currentTimeMillis();

    MockContextFactory.setAsInitial();
    context = new InitialContext();
    mockContainer = new MockContainer(context);

    MyJMSModule jmsModule = MyJMSModule.getInstance();

    context.rebind("jms/MockQueueConnectionFactory", jmsModule);
    context.rebind("jms-module", jmsModule);

    PrintStream systemOut = System.out;

    PollutionReducerPrintStream prOut = new PollutionReducerPrintStream(systemOut);
    prOut.setPollution("GLOBAL_SCOPE");
    prOut.setPollution("UTIL.FRAME");
    prOut.setPollution("FROMLOGSYS:");
    prOut.setPollution("MtepBase");
    prOut.setPollution("Local mode is being set");
    System.setOut(prOut);

    factory = DocumentBuilderFactory.newInstance();
    documentBuilder = factory.newDocumentBuilder();

    ExceptionHandler handler = new YFSExceptionHandler(1L, YFSException.class);
    ExceptionUtils.setHandler(handler);

    System.setProperty("vendor", "shell");
    System.setProperty("vendorFile",
        "/Users/jose/Documents/workspace/walmart/sterling/target/classes/resources/servers.properties");
    System.setProperty("DISABLE_DS_EXTENSIONS", "Y");
    System.setProperty("log4j.debug", "true");

    client = YIFClientFactoryImpl.getInstance().getApi("LOCAL");

    observer.observe(
        "Finished! Time to set up harness was " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
    observer.endObservation();
    System.out.println(SterlingClientTester.class.getClassLoader());
  }

  @Before
  public void setUp() throws Exception {

    environment = client.createEnvironment(createEnvironmentDocument());
  }

  // -----------------------------------------------------START TESTINg
  // AREA-------------------------------------------

  // ------------------Category Management-----------------------
  // @Test
  public void manageItems() throws Exception {
    Document responseDocument = client.manageItem(environment, manageDocument("category/category.manage:item*"));
    observer.observe(responseDocument);
  }

  // --------------------End of Category Management-------------------

  // --------------------Inventory Management-------------------
  // --------------------End of Inventory Managment-------------------

  // ------------------Selling Process------------------------

  @Test
  public void accept() throws Exception {
    System.out.println(getClass().getClassLoader());
    orderNumber = generateOrderNumber();
    Document responseDocument = client.createOrder(environment,
        manageDocument("selling/agent[fulfillment].accept:order.xml"));
    observer.observe(responseDocument);
  }

  @Test
  public void acceptNoAuth() throws Exception {
    orderNumber = generateOrderNumber();
    Document responseDocument = client.createOrder(environment,
        manageDocument("selling/order(payment-method\\charge\\@type='Authorization',code='').xml"));
    observer.observe(responseDocument);
  }

  // --------------------End of Selling Domain-------------------

  // --------------------Sourcing Management----------------------

  // -------------------End of Sourcing Management----------

  // ------------------Fulfillment Domain------------------------
  // @Test
  public void schedule() throws Exception {
    Document responseDocument = client.scheduleOrder(environment,
        manageDocument("fulfillment/task[schedule].process:order.xml"));
    observer.observe(responseDocument);
  }

  @Test
  public void testPaymentAuthorization() throws Exception {
    paymentTest = true;
    orderId = "2016010315383573513";

    try {
      Document responseDocument = client.processOrderPayments(environment, manageDocument(
          "finances/agent[sterling-oms].approve:agent[customer]\\@amount[requested]+agent[authority[{method}]].authorized:information[agent[client]\\payment[method]]+authority[{method}]+information[amount[requested]]+information[agent[client]].xml"));
      observer.observe(responseDocument);

      responseDocument = client.requestCollection(environment, manageDocument(
          "finances/agent[sterling-oms].approve:agent[customer]\\@amount[requested]+agent[authority[{method}]].authorized:information[agent[client]\\payment[method]]+authority[{method}]+information[amount[requested]]+information[agent[client]].xml"));
      observer.observe(responseDocument);
    } catch (Throwable e) {
      e.printStackTrace();
    }

  }

  // task[release].process:order*[eligible]
  @SuppressWarnings("rawtypes")
  @Test
  public void releaseJob() throws Exception {
    String whereString = " where ( ( YFS_TASK_Q.TRANSACTION_KEY = 'RELEASE.0001' ) )";
    YFS_Task_QueueDBHome home = YFS_Task_QueueDBHome.getInstance();
    YFSContext yfsContext = YFSContextManager.getInstance().getContextFor(environment);
    List list = home.listWithWhere((YFCDBContext) yfsContext, whereString, 40);
    observer.observe("size of releases is" + list.size());
    YFSReleaseOrderAgent releaser = new YFSReleaseOrderAgent();
    for (Object o : list) {
      YFS_Task_Queue task = (YFS_Task_Queue) o;
      Document taskDocument = documentBuilder.newDocument();
      Element root = taskDocument.createElement("TaskQ");
      root.setAttribute("TaskQKey", task.getTask_Q_Key());

      Element filters = taskDocument.createElement("TransactionFilters");
      root.appendChild(filters);

      filters.setAttribute("IgnoreReleaseDate", "Y");
      filters.setAttribute("CheckInventory", "Y");
      taskDocument.appendChild(root);

      releaser.executeTask((YFSEnvironment) yfsContext, (Document) taskDocument);
    }
    yfsContext.commit();
    yfsContext.close();
  }

  // -----------------------------------------------------End of
  // Fulfillment-------------------------------------------

  // --------------Finances Domain ---------------------
  // User driven? or event driven?

  // @Test
  public void removeHolds() throws Exception {
    Document template = manageDocument("templates/order-holdtype.xml");
    environment.setApiTemplate("getOrderList", template);
    Document orderListDocument = client.getOrderList(environment, manageDocument("order-list[query:holds].xml", false));
    environment.clearApiTemplate("getOrderlist");

    observer.observe(
        "Size of Order List that have holds= " + orderListDocument.getDocumentElement().getChildNodes().getLength());

    for (Element order : useCollection(orderListDocument.getElementsByTagName("Order"))) {
      for (Element holdType : useCollection(order.getElementsByTagName("OrderHoldType"))) {
        holdType.setAttribute("Status", "1300");
      }
      Document document = documentBuilder.newDocument();
      Node importedNode = document.importNode(order, true);
      document.appendChild(importedNode);

      client.changeOrder(environment, document);

    }

  }

  // -----------End Of Finance Domain------------------------------------

  // -----------------------------------------------------Start Distribution
  // process-----------------------------------

  public Document getOrders(Information information) throws Exception {
    Document template = manageDocument("templates/order-minimal.xml", false);
    environment.setApiTemplate("getOrderList", template);
    Document orderListDocument = client.getOrderList(environment, manageDocument(information.getInformation(), false));
    environment.clearApiTemplate("getOrderlist");
    return orderListDocument;

  }

  public Document getOrder(Information information) throws Exception {
    String id = information.get("id");
    Document orderSearch = manageDocument("order-list.xml", false);
    orderSearch.getDocumentElement().setAttribute("OrderHeaderKey", id);
    Document orderShipments = client.getShipmentListForOrder(environment, orderSearch);

    if (orderShipments.getDocumentElement().hasChildNodes()) {
      return null;
    }
    return orderShipments;
  }

  // @Test
  public void confirm() throws Exception {
    Document orderListDocument = getOrders(
        new Information("domain[*]\\agent[*].query:order*\\@enterprise-code='walmart-canada'"));

    for (Element order : useCollection(orderListDocument.getElementsByTagName("Order"))) {
      String orderId = order.getAttribute("OrderHeaderKey");
      @SuppressWarnings("unused")
      Document orderDocument = getOrder(
          new Information("domain[*]\\agent[*].query:order[sales,purchase]\\@id='" + orderId + "'"));

      Document orderReleaseListDocument = manageDocument("fulfillment/order-release.get.xml", false);
      Element orderReleaseList = orderReleaseListDocument.getDocumentElement();
      orderReleaseList.setAttribute("OrderHeaderKey", "" + order.getAttribute("OrderHeaderKey"));

      Document template = manageDocument("templates/order-release-list.xml", false);
      environment.setApiTemplate("getOrderReleaseList", template);

      Document orderReleasesDocument = client.getOrderReleaseList(environment, orderReleaseListDocument);
      environment.clearApiTemplate("getOrderReleaseList");

      if (orderReleasesDocument.getDocumentElement().hasChildNodes()) {
        String releaseNumber = "";
        String orderNumber = "";
        String shipNode = "";
        String itemId = "";
        String orderedQuantity = "";
        for (Element orderRelease : useCollection(
            orderReleasesDocument.getDocumentElement().getElementsByTagName("OrderRelease"))) {
          releaseNumber = orderRelease.getAttribute("ReleaseNo");
          shipNode = orderRelease.getAttribute("ShipNode");
          for (Element _order : useCollection(orderRelease.getElementsByTagName("Order"))) {
            orderNumber = _order.getAttribute("OrderNo");
            for (Element orderLine : useCollection(_order.getElementsByTagName("OrderLine"))) {
              orderedQuantity = orderLine.getAttribute("OrderedQty");
              for (Element item : useCollection(orderLine.getElementsByTagName("Item"))) {
                itemId = item.getAttribute("ItemID");
                break;
              }
            }
            break;
          }
        }
        Document shipmentDocument = manageDocument("distribution/shipment.xml");
        Element shipment = shipmentDocument.getDocumentElement();
        shipment.setAttribute("ActualShipmentDate", this.formatDate(Calendar.getInstance()));
        shipment.setAttribute("TrailerNo", "TRL-" + orderNumber);
        shipment.setAttribute("TrackingNo", "SHP-" + orderNumber);
        shipment.setAttribute("ProNo", "pro" + orderNumber);
        shipment.setAttribute("PickListNo", "PL-" + orderNumber);
        shipment.setAttribute("ManifestNo", "man-" + orderNumber);
        shipment.setAttribute("ShipNode", shipNode);

        for (Element container : useCollection(shipment.getElementsByTagName("Container"))) {
          container.setAttribute("TrackingNo", "CNT-" + orderNumber);
        }
        for (Element shipmentLine : useCollection(shipment.getElementsByTagName("ShipmentLine"))) {
          shipmentLine.setAttribute("OrderNo", "" + order.getAttribute("OrderNo"));
          shipmentLine.setAttribute("ReleaseNo", releaseNumber);
          shipmentLine.setAttribute("ItemID", itemId);
          shipmentLine.setAttribute("Quantity", orderedQuantity);
        }
        observer.observe(shipmentDocument);
        client.executeFlow(environment, "wmiShipmentUpdateConfirmShipment", shipmentDocument);
      }
    }
  }

  String trackingNumber = "CNT-429590907";

  // @Test
  public void testContainerStatusUpdate() throws Exception {
    Document template = this.manageDocument("templates/container-complete.xml");
    environment.setApiTemplate("getShipmentContainerList", template);

    Document containerSearch = this.manageDocument("distribution/container-list[query:tracking-number].xml", false);
    containerSearch.getDocumentElement().setAttribute("TrackingNo", trackingNumber);
    Document containerListDocument = client.getShipmentContainerList(environment, containerSearch);
    environment.clearApiTemplate("getShipmentContainerList");

    // logger.log(containerListDocument);
    String shipmentContainerKey = "";
    // String shipmentKey = "";
    // String containerNumber="";
    for (Element container : useCollection(
        containerListDocument.getDocumentElement().getElementsByTagName("Container"))) {
      shipmentContainerKey = container.getAttribute("ShipmentContainerKey");
      // shipmentKey = container.getAttribute("ShipmentKey");
      // containerNumber=container.getAttribute("ContainerNo");
    }
    Document containerUpdateDocument = manageDocument("distribution/carrier-update.xml", false);
    containerUpdateDocument.getDocumentElement().setAttribute("ShipmentContainerKey", shipmentContainerKey);
    // containerUpdateDocument.getDocumentElement().setAttribute("ShipmentKey",
    // shipmentKey);
    // containerUpdateDocument.getDocumentElement().setAttribute("ContainerNo",
    // containerNumber);
    // logger.log(containerUpdateDocument);

    Document responseDocument = client.changeShipmentContainer(environment, containerUpdateDocument);
    observer.observe(responseDocument);

  }

  @Test
  public void testShipmentContainerEntityStatusUpdate() throws Exception {
    YFS_Shipment_ContainerDBHome home = YFS_Shipment_ContainerDBHome.getInstance();
    String whereClause = "where  YFS_SHIPMENT_CONTAINER.tracking_no='" + trackingNumber + "'";

    YFSContext yfsContext = YFSContextManager.getInstance().getContextFor(environment);
    YFS_Shipment_Container container = home.selectWithWhere(yfsContext, whereClause);

    // boolean isPrevented = container.isPreventedField("Status");
    container.setStatus("1400.300");
    yfsContext.commit();
    yfsContext.close();

  }

  // -----------------------------------------------------END TESTING
  // AREA-------------------------------------------

  private String formatDate(Calendar calendar) {
    Format format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    return format.format(calendar.getTime());
  }

  private List<Element> useCollection(NodeList nodeList) {
    List<Element> elements = new ArrayList<Element>();
    for (int x = 0; x < nodeList.getLength(); x++) {
      elements.add((Element) nodeList.item(x));
    }
    return elements;
  }

  private static String generateOrderNumber() {
    Random randomGenerator = new Random();
    int random = randomGenerator.nextInt(1000000000);
    DecimalFormat formatter = new DecimalFormat("###########");
    return formatter.format(random);
  }

  private static Document createEnvironmentDocument() throws Exception {
    Document environmentDocument = documentBuilder.newDocument();
    Element environmentElement = environmentDocument.createElement("YFSEnvironment");
    environmentElement.setAttribute("userId", "tester");
    environmentElement.setAttribute("progId", "tester");
    environmentDocument.appendChild(environmentElement);

    return environmentDocument;
  }

  private void buildAndBind() throws Exception {
    for (String jndiName : jndiNames) {
      StringBuilder queueBuilder = new StringBuilder(jndiName);
      MyJMSModule jmsModule = (MyJMSModule) context.lookup("jms-module");
      jmsModule.buildQueue(queueBuilder.toString());
      com.mockrunner.mock.jms.MockQueue q = jmsModule.getQueue(queueBuilder.toString());
      jmsModule.registerTestMessageListenerForQueue(queueBuilder.toString(), new MyMessageListener());
      context.rebind(jndiName, q);
    }
  }

  private Document manageDocument(String fileName) throws Exception {
    return manageDocument(fileName, true);
  }

  boolean paymentTest;
  String orderId;

  private Document manageDocument(String fileName, boolean generateKey) throws Exception {
    InputStream in = SterlingClientTester.class.getClassLoader().getResourceAsStream(fileName);
    Document document = documentBuilder.parse(in);

    if (generateKey) {
      Element rootElement = document.getDocumentElement();

      if (rootElement.getTagName().contains("Order")) {

        if (paymentTest) {
          rootElement.setAttribute("OrderHeaderKey", orderId);
        } else {
          rootElement.setAttribute("OrderNo", orderNumber);

        }
        rootElement.setAttribute("DocumentType", "0001");
        rootElement.setAttribute("EnterpriseCode", "Walmart_Canada_Corp");
        rootElement.setAttribute("OrderDate", this.formatDate(Calendar.getInstance()));
      }
    }
    return document;
  }

  @After
  public void tearDown() throws Exception {
  }

  @AfterClass
  public static void oneTimeTearDown() {
    // observer.finalize();
  }

  public static void main(String a[]) throws Exception {
    System.out.println(SterlingClientTester.class.getClassLoader().getClass().getName());
    SterlingClientTester tester = new SterlingClientTester();
    tester.oneTimeSetUp();
    tester.setUp();
    tester.accept();
    tester.tearDown();

  }

  // ------------------------------------DMZ
  // Zone---------------------------------
  // @Test
  // public void release() throws Exception
  // {
  // Document responseDocument = client.releaseOrder(environment,
  // manageDocument("fulfillment/order-release.xml"));
  // writeOutDocument(responseDocument);
  // }

  // @Test
  // public void testQueryingService() throws Exception
  // {
  // YFSContext yfsContext =
  // YFSContextManager.getInstance().getContextFor(environment);
  //
  // String emailId = "ajaisw2@test.com";
  // QueryingService service = new SterlingOMSQueryingService(yfsContext);
  // ComplexQuery query = new ComplexQuery();
  // query.setVariableValue(emailId);
  // query.setFacts("by email.id");
  // service.countofOrdersDeliveredForCustomer(query);
  //
  // String orderLineKey = "2016010315401573569";
  // query.setVariableValue(orderLineKey);
  // query.setFacts("by order-line.id");
  // service.countofOrdersDeliveredForCustomer(query);
  // }

  // @Test
  // public void groceryCreate() throws Exception
  // {
  // Document responseDocument = client.createOrder(environment,
  // manageDocument("selling/groceries/order.xml"));
  // writeOutDocument(responseDocument);
  // }

  // -Djava.system.class.loader=com.trivnew.classloader.TrivnewClassLoader
}
