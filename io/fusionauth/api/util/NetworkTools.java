package io.fusionauth.api.util;

import com.inversoft.validator.IPAddressType;
import com.inversoft.validator.IPValidator;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.domain.SystemConfiguration;
import io.fusionauth.domain.SystemTrustedProxyConfigurationPolicy;
import io.fusionauth.http.server.HTTPRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkTools {
  private static final Pattern PERIOD = Pattern.compile("\\.");
  
  private static final Logger logger = LoggerFactory.getLogger(NetworkTools.class);
  
  public static long convertIPv4ToLong(String paramString) {
    Objects.requireNonNull(paramString);
    long l = 0L;
    String[] arrayOfString = PERIOD.split(paramString);
    for (byte b = 3; b >= 0; b--) {
      long l1 = Long.parseLong(arrayOfString[3 - b]);
      l |= l1 << b * 8;
    } 
    return l;
  }
  
  public static Map<String, List<InetAddress>> getIpv4HostAddresses() {
    LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
    try {
      Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
      while (enumeration.hasMoreElements()) {
        NetworkInterface networkInterface = enumeration.nextElement();
        if (networkInterface.isUp() && !networkInterface.isLoopback()) {
          Enumeration<InetAddress> enumeration1 = networkInterface.getInetAddresses();
          while (enumeration1.hasMoreElements()) {
            InetAddress inetAddress = enumeration1.nextElement();
            if (inetAddress instanceof java.net.Inet4Address)
              ((List<InetAddress>)linkedHashMap.computeIfAbsent(networkInterface.getName(), paramString -> new ArrayList())).add(inetAddress); 
          } 
        } 
      } 
    } catch (Exception exception) {
      logger.debug("Exception: ", exception);
    } 
    return (Map)linkedHashMap;
  }
  
  public static String getTrustedClientIPAddress(HTTPRequest paramHTTPRequest, List<CIDRBlock> paramList) {
    String str1 = paramHTTPRequest.getRawIPAddress();
    if (paramList == null || paramList.isEmpty())
      return str1; 
    List list = paramHTTPRequest.getHeaders("X-Forwarded-For");
    if (list == null || list.isEmpty())
      return str1; 
    String str2 = null;
    try {
      str2 = str1;
      if (isTrustedProxy(str2, paramList)) {
        ArrayList<String> arrayList = new ArrayList();
        for (String str : list) {
          String[] arrayOfString = str.split(",");
          for (String str3 : arrayOfString)
            arrayList.add(str3.trim()); 
        } 
        for (int i = arrayList.size() - 1; i >= 0; i--) {
          str2 = arrayList.get(i);
          if (!isTrustedProxy(str2, paramList))
            break; 
        } 
      } 
    } catch (Exception exception) {}
    if (str2 != null)
      return str2; 
    return str1;
  }
  
  public static String getTrustedClientIPAddress(HTTPRequest paramHTTPRequest, FusionAuthConfiguration paramFusionAuthConfiguration, SystemConfiguration paramSystemConfiguration) {
    if (paramSystemConfiguration.trustedProxyConfiguration.trustPolicy == SystemTrustedProxyConfigurationPolicy.All)
      return paramHTTPRequest.getIPAddress(); 
    ArrayList<String> arrayList = new ArrayList();
    arrayList.addAll(List.of((Object[])paramFusionAuthConfiguration.trustedProxies()));
    arrayList.addAll(paramSystemConfiguration.trustedProxyConfiguration.trusted);
    return getTrustedClientIPAddress(paramHTTPRequest, arrayList.stream().map(CIDRBlock::new).toList());
  }
  
  public static String guessSiteIpAddress() {
    LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
    try {
      Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
      while (enumeration.hasMoreElements()) {
        NetworkInterface networkInterface = enumeration.nextElement();
        if (networkInterface.isUp()) {
          Enumeration<InetAddress> enumeration1 = networkInterface.getInetAddresses();
          while (enumeration1.hasMoreElements()) {
            InetAddress inetAddress = enumeration1.nextElement();
            if (inetAddress instanceof java.net.Inet4Address && inetAddress.isSiteLocalAddress())
              linkedHashMap.put(networkInterface.getName(), inetAddress.getHostAddress()); 
          } 
        } 
      } 
      if (linkedHashMap.size() > 0) {
        for (byte b = 0; b < 10; b++) {
          if (linkedHashMap.containsKey("en" + b))
            return (String)linkedHashMap.get("en" + b); 
        } 
        return (String)((Map.Entry)linkedHashMap.entrySet().iterator().next()).getValue();
      } 
    } catch (Exception exception) {
      logger.error("Failed to retrieve the first site local address, fall back to localhost");
      logger.debug("Exception: ", exception);
    } 
    return "localhost";
  }
  
  public static boolean isLoopbackAddress(String paramString) {
    try {
      InetAddress inetAddress = InetAddress.getByName(paramString);
      return inetAddress.isLoopbackAddress();
    } catch (Exception exception) {
      return false;
    } 
  }
  
  public static boolean isValidCDIR(String paramString) {
    try {
      new CIDRBlock(paramString);
      return true;
    } catch (Exception exception) {
      return false;
    } 
  }
  
  public static void main(String[] paramArrayOfString) {
    if (paramArrayOfString.length > 0 && 
      paramArrayOfString[0].equals("site-address"))
      System.out.println("Guess Site Address: " + guessSiteIpAddress()); 
  }
  
  public static String parseXForwardedFor(String paramString) {
    int i = paramString.indexOf(',');
    return (i == -1) ? paramString.trim() : paramString.substring(0, i).trim();
  }
  
  public static String sanitizeIPAddress(String paramString) {
    if (paramString == null || paramString.isEmpty())
      return null; 
    paramString = parseXForwardedFor(paramString);
    IPAddressType iPAddressType = IPValidator.getType(paramString);
    if (iPAddressType != IPAddressType.IPv4 && iPAddressType != IPAddressType.IPv6)
      return null; 
    return paramString;
  }
  
  private static boolean isTrustedProxy(String paramString, List<CIDRBlock> paramList) {
    for (CIDRBlock cIDRBlock : paramList) {
      if (cIDRBlock.contains(paramString))
        return true; 
    } 
    return false;
  }
}
