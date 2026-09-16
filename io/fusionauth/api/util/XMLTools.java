package io.fusionauth.api.util;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XMLTools {
  public static String prettyPrint(String paramString) {
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty("indent", "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      StreamResult streamResult = new StreamResult(new StringWriter());
      transformer.transform(new StreamSource(new StringReader(paramString)), streamResult);
      return streamResult.getWriter().toString();
    } catch (Exception exception) {
      return paramString;
    } 
  }
}
