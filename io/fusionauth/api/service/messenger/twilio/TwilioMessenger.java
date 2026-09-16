package io.fusionauth.api.service.messenger.twilio;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.domain.message.SendMessageResult;
import io.fusionauth.api.service.messaging.InvalidMessageLength;
import io.fusionauth.api.service.messaging.TwilioPushException;
import io.fusionauth.api.service.messenger.Messenger;
import io.fusionauth.api.service.messenger.MessengerException;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.util.PhoneNumberTools;
import io.fusionauth.domain.message.Message;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.TwilioMessengerConfiguration;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TwilioMessenger implements Messenger {
  private static final Logger logger = LoggerFactory.getLogger(TwilioMessenger.class);
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  private final TwilioRESTClient twilioClient;
  
  @Inject
  public TwilioMessenger(ProxyInfoSupplier paramProxyInfoSupplier, TwilioRESTClient paramTwilioRESTClient) {
    this.proxyInfoSupplier = paramProxyInfoSupplier;
    this.twilioClient = paramTwilioRESTClient;
  }
  
  private static String createVoiceTwiml(String paramString1, @Nullable String paramString2) {
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
      Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
      Element element1 = document.createElement("Response");
      document.appendChild(element1);
      Element element2 = document.createElement("Pause");
      element2.setAttribute("length", "3");
      element1.appendChild(element2);
      Element element3 = document.createElement("Say");
      element3.setTextContent(paramString1);
      if (paramString2 != null)
        element3.setAttribute("language", paramString2); 
      element1.appendChild(element3);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty("omit-xml-declaration", "yes");
      transformer.setOutputProperty("indent", "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      StringWriter stringWriter = new StringWriter();
      transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
      return stringWriter.toString();
    } catch (Exception exception) {
      throw new TwilioPushException(exception, new Object[0]);
    } 
  }
  
  private static SendMessageResult handleResponse(TwilioMessengerConfiguration paramTwilioMessengerConfiguration, Debugger paramDebugger, ClientResponse<String, String> paramClientResponse) {
    if (!paramClientResponse.wasSuccessful()) {
      if (paramClientResponse.exception != null) {
        paramDebugger.log("The response was not successful");
        logger.error("Twilio Messenger {} [{}] response was not successful", new Object[] { paramTwilioMessengerConfiguration.name, paramTwilioMessengerConfiguration.url, paramClientResponse.exception });
        paramDebugger.log(paramClientResponse.exception);
        throw new MessengerException(paramTwilioMessengerConfiguration.name, paramTwilioMessengerConfiguration.url, paramClientResponse.exception);
      } 
      paramDebugger.log("The response was not successful: [" + (String)paramClientResponse.errorResponse + "]");
      logger.error("Twilio Messenger {} [{}] returned response code [{}], message [{}]", new Object[] { paramTwilioMessengerConfiguration.name, paramTwilioMessengerConfiguration.url, 

            
            Integer.valueOf(paramClientResponse.status), paramClientResponse.errorResponse });
      throw new MessengerException(paramTwilioMessengerConfiguration.name, paramTwilioMessengerConfiguration.url, (String)paramClientResponse.errorResponse, paramClientResponse.status);
    } 
    return new SendMessageResult(paramClientResponse.status);
  }
  
  private static String toE164format(String paramString) {
    try {
      return PhoneNumberTools.toE164format(paramString);
    } catch (NumberParseException numberParseException) {
      throw new TwilioPushException(numberParseException, new Object[0]);
    } 
  }
  
  public SendMessageResult send(Message paramMessage, BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    // Byte code:
    //   0: aload_2
    //   1: checkcast io/fusionauth/domain/messenger/TwilioMessengerConfiguration
    //   4: astore_3
    //   5: new io/fusionauth/api/service/system/eventLog/Debugger
    //   8: dup
    //   9: aload_3
    //   10: getfield debug : Z
    //   13: aload_3
    //   14: invokevirtual getType : ()Lio/fusionauth/domain/messenger/MessengerType;
    //   17: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   20: aload_3
    //   21: getfield name : Ljava/lang/String;
    //   24: aload_3
    //   25: getfield id : Ljava/util/UUID;
    //   28: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   31: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   36: invokespecial <init> : (ZLjava/lang/String;)V
    //   39: astore #4
    //   41: aload_1
    //   42: dup
    //   43: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
    //   46: pop
    //   47: astore #5
    //   49: iconst_0
    //   50: istore #6
    //   52: aload #5
    //   54: iload #6
    //   56: <illegal opcode> typeSwitch : (Lio/fusionauth/domain/message/Message;I)I
    //   61: lookupswitch default -> 198, 0 -> 88, 1 -> 155
    //   88: aload #5
    //   90: checkcast io/fusionauth/domain/message/voice/VoiceMessage
    //   93: astore #7
    //   95: aload #4
    //   97: aload #7
    //   99: getfield phoneNumber : Ljava/lang/String;
    //   102: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;)Ljava/lang/String;
    //   107: invokevirtual log : (Ljava/lang/String;)Lio/fusionauth/api/service/system/eventLog/Debugger;
    //   110: pop
    //   111: aload #7
    //   113: getfield locale : Ljava/util/Locale;
    //   116: ifnull -> 130
    //   119: aload #7
    //   121: getfield locale : Ljava/util/Locale;
    //   124: invokevirtual toLanguageTag : ()Ljava/lang/String;
    //   127: goto -> 131
    //   130: aconst_null
    //   131: astore #8
    //   133: aload_0
    //   134: aload #7
    //   136: getfield phoneNumber : Ljava/lang/String;
    //   139: aload #7
    //   141: getfield message : Ljava/lang/String;
    //   144: aload #8
    //   146: aload_3
    //   147: aload #4
    //   149: invokevirtual makeVoiceCall : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lio/fusionauth/domain/messenger/TwilioMessengerConfiguration;Lio/fusionauth/api/service/system/eventLog/Debugger;)Lio/fusionauth/api/domain/message/SendMessageResult;
    //   152: goto -> 220
    //   155: aload #5
    //   157: checkcast io/fusionauth/domain/message/sms/SMSMessage
    //   160: astore #8
    //   162: aload #4
    //   164: aload #8
    //   166: getfield phoneNumber : Ljava/lang/String;
    //   169: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;)Ljava/lang/String;
    //   174: invokevirtual log : (Ljava/lang/String;)Lio/fusionauth/api/service/system/eventLog/Debugger;
    //   177: pop
    //   178: aload_0
    //   179: aload #8
    //   181: getfield phoneNumber : Ljava/lang/String;
    //   184: aload #8
    //   186: getfield textMessage : Ljava/lang/String;
    //   189: aload_3
    //   190: aload #4
    //   192: invokevirtual sendTextMessage : (Ljava/lang/String;Ljava/lang/String;Lio/fusionauth/domain/messenger/TwilioMessengerConfiguration;Lio/fusionauth/api/service/system/eventLog/Debugger;)Lio/fusionauth/api/domain/message/SendMessageResult;
    //   195: goto -> 220
    //   198: new java/lang/IllegalArgumentException
    //   201: dup
    //   202: aload_1
    //   203: invokeinterface getType : ()Lio/fusionauth/domain/message/MessageType;
    //   208: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   211: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;)Ljava/lang/String;
    //   216: invokespecial <init> : (Ljava/lang/String;)V
    //   219: athrow
    //   220: astore #5
    //   222: aload #4
    //   224: invokevirtual done : ()V
    //   227: aload #5
    //   229: areturn
    //   230: astore #9
    //   232: aload #4
    //   234: invokevirtual done : ()V
    //   237: aload #9
    //   239: athrow
    // Line number table:
    //   Java source line number -> byte code offset
    //   #135	-> 0
    //   #139	-> 5
    //   #142	-> 41
    //   #143	-> 88
    //   #144	-> 95
    //   #145	-> 111
    //   #146	-> 133
    //   #148	-> 155
    //   #149	-> 162
    //   #150	-> 178
    //   #152	-> 198
    //   #153	-> 220
    //   #155	-> 222
    //   #142	-> 227
    //   #155	-> 230
    //   #156	-> 237
    // Exception table:
    //   from	to	target	type
    //   41	222	230	finally
    //   230	232	230	finally
  }
  
  private SendMessageResult makeVoiceCall(String paramString1, String paramString2, @Nullable String paramString3, TwilioMessengerConfiguration paramTwilioMessengerConfiguration, Debugger paramDebugger) {
    HashMap<Object, Object> hashMap;
    try {
      hashMap = new HashMap<>(Map.of("To", List.of(toE164format(paramString1)), "From", 
            List.of(toE164format(paramTwilioMessengerConfiguration.fromPhoneNumber)), "Twiml", 
            List.of(createVoiceTwiml(paramString2, paramString3))));
    } catch (TwilioPushException twilioPushException) {
      logger.error("Exception when creating request. This is likely an issue with a missing 'from' or 'to' phone number, or an invalid message. From number [{}], to number [{}]", new Object[] { paramTwilioMessengerConfiguration.fromPhoneNumber, paramString1, twilioPushException });
      throw new MessengerException(paramTwilioMessengerConfiguration.name, paramTwilioMessengerConfiguration.url, twilioPushException);
    } 
    paramDebugger.log("Invoking client call to Twilio with accountSID [" + paramTwilioMessengerConfiguration.accountSID + "], url [" + String.valueOf(paramTwilioMessengerConfiguration.url) + "] and message [" + paramString2 + "].");
    ClientResponse<String, String> clientResponse = this.twilioClient.makeCall(this.proxyInfoSupplier, (Map)hashMap, paramTwilioMessengerConfiguration.accountSID, paramTwilioMessengerConfiguration.authToken, paramTwilioMessengerConfiguration.url);
    return handleResponse(paramTwilioMessengerConfiguration, paramDebugger, clientResponse);
  }
  
  private SendMessageResult sendTextMessage(String paramString1, String paramString2, TwilioMessengerConfiguration paramTwilioMessengerConfiguration, Debugger paramDebugger) {
    HashMap<Object, Object> hashMap;
    if (paramString2.length() > 1600) {
      paramDebugger.log("The message exceeded the maximum message length (1600) allowed by Twilio");
      throw new InvalidMessageLength();
    } 
    try {
      hashMap = new HashMap<>(Map.of("To", List.of(toE164format(paramString1)), "Body", 
            List.of(paramString2)));
      if (paramTwilioMessengerConfiguration.messagingServiceSid != null) {
        hashMap.put("MessagingServiceSid", List.of(paramTwilioMessengerConfiguration.messagingServiceSid));
      } else {
        hashMap.put("From", List.of(toE164format(paramTwilioMessengerConfiguration.fromPhoneNumber)));
      } 
    } catch (TwilioPushException twilioPushException) {
      logger.error("Exception when creating request. This is likely an issue with a missing 'from' or 'to' phone number. From number [{}], to number [{}]", new Object[] { paramTwilioMessengerConfiguration.fromPhoneNumber, paramString1, twilioPushException });
      throw new MessengerException(paramTwilioMessengerConfiguration.name, paramTwilioMessengerConfiguration.url, twilioPushException);
    } 
    paramDebugger.log("Invoking client call to Twilio with accountSID [" + paramTwilioMessengerConfiguration.accountSID + "], url [" + String.valueOf(paramTwilioMessengerConfiguration.url) + "] and message [" + paramString2 + "].");
    ClientResponse<String, String> clientResponse = this.twilioClient.sendSMS(this.proxyInfoSupplier, (Map)hashMap, paramTwilioMessengerConfiguration.accountSID, paramTwilioMessengerConfiguration.authToken, paramTwilioMessengerConfiguration.url);
    return handleResponse(paramTwilioMessengerConfiguration, paramDebugger, clientResponse);
  }
}
