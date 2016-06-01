package oms.framework.transformer;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class DocumentToStringTransformer {

  public String transform(Document doc) {

    String output = null;
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      javax.xml.transform.Transformer transformer;

      transformer = tf.newTransformer();

      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(doc), new StreamResult(writer));
      output = writer.getBuffer().toString(); // .replaceAll("\n|\r", "");

    } catch (TransformerConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (javax.xml.transform.TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return output;
  }

}
