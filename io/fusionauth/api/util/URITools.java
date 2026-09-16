package io.fusionauth.api.util;

import com.inversoft.validator.IPValidator;
import io.fusionauth.api.service.system.SystemDefaultsSingleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class URITools {
  private static final char[] HEX = "0123456789ABCDEF".toCharArray();
  
  private static final String PublicSuffixDataPath = "/io/fusionauth/api/util/public_suffix_list.dat";
  
  private static final Set<String> PublicSuffixes = new HashSet<>();
  
  private static final Set<Character> UnreservedChars = new HashSet<>(Arrays.asList(new Character[] { 
          Character.valueOf('A'), Character.valueOf('B'), Character.valueOf('C'), Character.valueOf('D'), Character.valueOf('E'), Character.valueOf('F'), Character.valueOf('G'), Character.valueOf('H'), Character.valueOf('I'), Character.valueOf('J'), 
          Character.valueOf('K'), Character.valueOf('L'), Character.valueOf('M'), Character.valueOf('N'), Character.valueOf('O'), Character.valueOf('P'), Character.valueOf('Q'), Character.valueOf('R'), Character.valueOf('S'), Character.valueOf('T'), 
          Character.valueOf('U'), Character.valueOf('V'), Character.valueOf('W'), Character.valueOf('X'), Character.valueOf('Y'), Character.valueOf('Z'), 
          Character.valueOf('a'), Character.valueOf('b'), Character.valueOf('c'), Character.valueOf('d'), 
          Character.valueOf('e'), Character.valueOf('f'), Character.valueOf('g'), Character.valueOf('h'), Character.valueOf('i'), Character.valueOf('j'), Character.valueOf('k'), Character.valueOf('l'), Character.valueOf('m'), Character.valueOf('n'), 
          Character.valueOf('o'), Character.valueOf('p'), Character.valueOf('q'), Character.valueOf('r'), Character.valueOf('s'), Character.valueOf('t'), Character.valueOf('u'), Character.valueOf('v'), Character.valueOf('w'), Character.valueOf('x'), 
          Character.valueOf('y'), Character.valueOf('z'), 
          Character.valueOf('0'), Character.valueOf('1'), Character.valueOf('2'), Character.valueOf('3'), Character.valueOf('4'), Character.valueOf('5'), Character.valueOf('6'), Character.valueOf('7'), 
          Character.valueOf('8'), Character.valueOf('9'), 
          Character.valueOf('-'), Character.valueOf('_'), Character.valueOf('.'), Character.valueOf('~') }));
  
  private static final Set<String> WildcardSuffixes = new HashSet<>();
  
  public static boolean anyMatch(URI paramURI, List<URI> paramList) {
    return paramList.stream()
      .map(URITools::buildPattern)
      .anyMatch(paramPattern -> paramPattern.matcher(paramURI.toString()).matches());
  }
  
  public static Pattern buildPattern(URI paramURI) {
    StringBuilder stringBuilder = new StringBuilder();
    boolean bool = false;
    String str1 = paramURI.toString();
    if (str1.contains(":*")) {
      bool = true;
      str1 = str1.replace(":*", "");
    } 
    String str2 = UUID.randomUUID().toString();
    URI uRI = URI.create(str1.replace("*", str2));
    stringBuilder.append(uRI.getScheme()).append("://");
    if (uRI.getRawUserInfo() != null)
      stringBuilder.append(uRI.getRawUserInfo()).append("@"); 
    stringBuilder.append(uRI.getHost()
        .replace(".", "\\.")
        .replace(str2, Matcher.quoteReplacement("[^.:/?]+")));
    if (bool) {
      stringBuilder.append(":").append("\\d{1,}");
    } else if (uRI.getPort() > 0) {
      stringBuilder.append(":").append(uRI.getPort());
    } 
    if (uRI.getRawPath() != null)
      stringBuilder.append(uRI.getRawPath()
          .replace(".", "\\.")
          .replaceAll(str2, Matcher.quoteReplacement("[^/?]+"))); 
    if (uRI.getRawQuery() != null) {
      stringBuilder.append("\\?");
      stringBuilder.append(uRI.getRawQuery()
          .replace(".", "\\.")
          .replaceAll(str2, Matcher.quoteReplacement("[^&]+")));
    } 
    return Pattern.compile(stringBuilder.toString());
  }
  
  public static String encodeURIComponent(String paramString) {
    StringBuilder stringBuilder = new StringBuilder();
    for (byte b : paramString.getBytes(StandardCharsets.UTF_8)) {
      if (isSafe(b)) {
        stringBuilder.append((char)b);
      } else {
        stringBuilder.append('%');
        stringBuilder.append(HEX[(b & 0xF0) >> 4]);
        stringBuilder.append(HEX[b & 0xF]);
      } 
    } 
    return stringBuilder.toString();
  }
  
  public static URI fromURL(URL paramURL) {
    try {
      return paramURL.toURI();
    } catch (URISyntaxException uRISyntaxException) {
      throw new RuntimeException(uRISyntaxException);
    } 
  }
  
  public static String getApexDomain(URI paramURI) {
    if (paramURI == null)
      return null; 
    String str = paramURI.getHost();
    if (str == null) {
      try {
        str = paramURI.toURL().getHost();
      } catch (MalformedURLException malformedURLException) {}
      if (str == null)
        return null; 
    } 
    if (IPValidator.isValidIPv4(str) || IPValidator.isValidIPv6(str))
      return str; 
    CharSequence charSequence = null;
    while (true) {
      String[] arrayOfString = str.split("\\.", 2);
      if (arrayOfString.length == 1)
        return arrayOfString[0]; 
      if (PublicSuffixes.contains(arrayOfString[1]))
        return String.join(".", new CharSequence[] { arrayOfString[0], arrayOfString[1] }); 
      if (WildcardSuffixes.contains(String.join(".", new CharSequence[] { "*", arrayOfString[1] }))) {
        if (charSequence != null)
          return String.join(".", new CharSequence[] { charSequence, arrayOfString[0], arrayOfString[1] }); 
        return String.join(".", new CharSequence[] { arrayOfString[0], arrayOfString[1] });
      } 
      charSequence = arrayOfString[0];
      str = arrayOfString[1];
    } 
  }
  
  public static boolean hostDomainsEqual(URI paramURI1, URI paramURI2) {
    if (paramURI1 == null || paramURI2 == null)
      return false; 
    String str1 = getApexDomain(paramURI1);
    String str2 = getApexDomain(paramURI2);
    if (str1 == null || str2 == null)
      return false; 
    return str1.equalsIgnoreCase(str2);
  }
  
  public static boolean isSameBaseURL(String paramString1, String paramString2) {
    if (paramString1 == null || paramString2 == null)
      return false; 
    try {
      URI uRI1 = URI.create(paramString1);
      URI uRI2 = URI.create(paramString2);
      return isSameBaseURL(uRI1, uRI2);
    } catch (Exception exception) {
      return false;
    } 
  }
  
  public static boolean isSameBaseURL(URI paramURI1, URI paramURI2) {
    if (paramURI1 == null || paramURI2 == null)
      return false; 
    try {
      return (paramURI1.getHost().equalsIgnoreCase(paramURI2.getHost()) && paramURI1
        .getScheme().equalsIgnoreCase(paramURI2.getScheme()) && paramURI1
        .getPort() == paramURI2.getPort());
    } catch (Exception exception) {
      return false;
    } 
  }
  
  public static boolean isSameBaseURLAndPath(@Nullable String paramString1, @Nullable String paramString2) {
    if (paramString1 == null || paramString2 == null)
      return false; 
    try {
      URI uRI1 = URI.create(paramString1);
      URI uRI2 = URI.create(paramString2);
      return (isSameBaseURL(uRI1, uRI2) && uRI1.getPath().equals(uRI2.getPath()));
    } catch (Exception exception) {
      return false;
    } 
  }
  
  public static String loadResourceByURI(Class<?> paramClass, String paramString) {
    try {
      InputStream inputStream = paramClass.getResourceAsStream(paramString);
      try {
        if (inputStream == null)
          throw new RuntimeException("Resource not found at URI [" + paramString + "]"); 
        StringBuilder stringBuilder = new StringBuilder();
        byte[] arrayOfByte = new byte[8192];
        int i;
        while ((i = inputStream.read(arrayOfByte)) > 0)
          stringBuilder.append(new String(arrayOfByte, 0, i, StandardCharsets.UTF_8)); 
        String str = stringBuilder.toString();
        if (inputStream != null)
          inputStream.close(); 
        return str;
      } catch (Throwable throwable) {
        if (inputStream != null)
          try {
            inputStream.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException iOException) {
      throw new RuntimeException("Unknown or invalid resource classpath entry [" + paramString + "]", iOException);
    } 
  }
  
  public static URI sanitizeOrigin(String paramString) {
    URI uRI = URI.create(paramString);
    try {
      return new URI(uRI.getScheme(), null, uRI.getHost(), uRI.getPort(), null, null, null);
    } catch (URISyntaxException uRISyntaxException) {
      return uRI;
    } 
  }
  
  public static boolean validateURIPattern(URI paramURI) {
    if (paramURI == null)
      return true; 
    String str1 = paramURI.toString();
    if (str1.contains(":*"))
      str1 = str1.replace(":*", ""); 
    String str2 = UUID.randomUUID().toString();
    URI uRI = URI.create(str1.replace("*", str2));
    String str3 = uRI.getHost();
    if (str3 != null) {
      if (countOccurrences(str3, str2) > 1)
        return false; 
      String[] arrayOfString = str3.split("\\.");
      if (arrayOfString.length < 3 && arrayOfString[0].contains(str2))
        return false; 
      if (Arrays.<String>stream(arrayOfString).skip(1L).anyMatch(paramString2 -> paramString2.contains(paramString1)))
        return false; 
    } 
    String str4 = paramURI.getAuthority();
    if (str4 != null) {
      int i = str4.indexOf(":");
      if (str3 == null) {
        String str = (i == -1) ? str4 : str4.substring(0, i);
        if (str.contains("*"))
          return false; 
      } 
      if (i > 0) {
        String str = str4.substring(i + 1);
        if (str.contains("*") && !str.equals("*"))
          return false; 
      } 
    } 
    if (paramURI.getPath() != null && 
      Arrays.<String>stream(paramURI.getPath().split("/"))
      .anyMatch(paramString -> (countOccurrences(paramString, "*") > 1)))
      return false; 
    if (paramURI.getQuery() != null && 
      Arrays.<String>stream(paramURI.getQuery().split("&"))
      .map(paramString -> paramString.split("=", 2)).anyMatch(paramArrayOfString -> !validateQueryParameterPattern(paramArrayOfString)))
      return false; 
    return true;
  }
  
  private static int countOccurrences(String paramString1, String paramString2) {
    byte b = 0;
    int i = 0;
    while ((i = paramString1.indexOf(paramString2, i)) != -1) {
      i += paramString2.length();
      b++;
    } 
    return b;
  }
  
  private static boolean isSafe(byte paramByte) {
    return UnreservedChars.contains(Character.valueOf((char)paramByte));
  }
  
  private static boolean validateQueryParameterPattern(String[] paramArrayOfString) {
    if (paramArrayOfString.length == 0)
      return true; 
    if (paramArrayOfString[0].contains("*"))
      return false; 
    if (paramArrayOfString.length > 1)
      return (paramArrayOfString[1].equals("*") || !paramArrayOfString[1].contains("*")); 
    return true;
  }
  
  private static class URIPatterns {
    public static final String HOST = "[^.:/?]+";
    
    public static final String PATH = "[^/?]+";
    
    public static final String PORT = "\\d{1,}";
    
    public static final String QUERY_PARAMETER = "[^&]+";
  }
  
  static {
    String str = loadResourceByURI(SystemDefaultsSingleton.class, "/io/fusionauth/api/util/public_suffix_list.dat");
    Arrays.<String>stream(str.split("\n"))
      .forEach(paramString -> {
          paramString = paramString.trim();
          if (paramString.startsWith("*")) {
            WildcardSuffixes.add(paramString);
          } else if (!paramString.startsWith("//") && !paramString.startsWith("!") && !paramString.isEmpty()) {
            PublicSuffixes.add(paramString);
          } 
        });
  }
}
