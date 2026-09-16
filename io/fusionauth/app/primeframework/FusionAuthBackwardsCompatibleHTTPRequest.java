package io.fusionauth.app.primeframework;

import io.fusionauth.http.Cookie;
import io.fusionauth.http.FileInfo;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FusionAuthBackwardsCompatibleHTTPRequest extends HTTPRequest {
  private static final RuntimeException UnsupportedException = new UnsupportedOperationException("This method is not available in a themed template.");
  
  private final HTTPRequest request;
  
  public FusionAuthBackwardsCompatibleHTTPRequest(HTTPRequest paramHTTPRequest) {
    this.request = paramHTTPRequest;
  }
  
  public Object getAttribute(String paramString) {
    return this.request.getAttribute(paramString);
  }
  
  public Set<String> getAttributeNames() {
    return getAttributes().keySet();
  }
  
  public Map<String, Object> getAttributes() {
    return this.request.getAttributes();
  }
  
  public String getBaseURL() {
    return this.request.getBaseURL();
  }
  
  public byte[] getBodyBytes() {
    throw UnsupportedException;
  }
  
  public Charset getCharacterEncoding() {
    return this.request.getCharacterEncoding();
  }
  
  public Long getContentLength() {
    throw UnsupportedException;
  }
  
  public String getContentType() {
    return this.request.getContentType();
  }
  
  public String getContextPath() {
    return this.request.getContextPath();
  }
  
  public Cookie getCookie(String paramString) {
    return this.request.getCookie(paramString);
  }
  
  public List<Cookie> getCookies() {
    ArrayList<Cookie> arrayList = new ArrayList(this.request.getCookies());
    arrayList.sort(Comparator.comparing(paramCookie -> paramCookie.name));
    return arrayList;
  }
  
  public Instant getDateHeader(String paramString) {
    return this.request.getDateHeader(paramString);
  }
  
  public List<FileInfo> getFiles() {
    throw UnsupportedException;
  }
  
  public String getHeader(String paramString) {
    return this.request.getHeader(paramString);
  }
  
  public Set<String> getHeaderNames() {
    return this.request.getHeaders().keySet();
  }
  
  public List<String> getHeaders(String paramString) {
    return this.request.getHeaders(paramString);
  }
  
  public Map<String, List<String>> getHeaders() {
    return this.request.getHeaders();
  }
  
  public Map<String, List<String>> getHeadersMap() {
    return this.request.getHeaders();
  }
  
  public String getHost() {
    return this.request.getHost();
  }
  
  public String getIPAddress() {
    return this.request.getIPAddress();
  }
  
  public int getIntHeader(String paramString) {
    String str = this.request.getHeader(paramString);
    if (str == null)
      return -1; 
    return Integer.parseInt(str.toString());
  }
  
  public int getLocalPort() {
    return this.request.getPort();
  }
  
  public Locale getLocale() {
    return this.request.getLocale();
  }
  
  public List<Locale> getLocales() {
    return this.request.getLocales();
  }
  
  public HTTPMethod getMethod() {
    return this.request.getMethod();
  }
  
  public String getParameter(String paramString) {
    return getParameterValue(paramString);
  }
  
  public Map<String, List<String>> getParameterMap() {
    return getParameters();
  }
  
  public Set<String> getParameterNames() {
    return getParameters().keySet();
  }
  
  public String getParameterValue(String paramString) {
    return this.request.getParameter(paramString);
  }
  
  public List<String> getParameterValues(String paramString) {
    return this.request.getParameters(paramString);
  }
  
  public Map<String, List<String>> getParameters() {
    return this.request.getParameters();
  }
  
  public String getPath() {
    return this.request.getPath();
  }
  
  public int getPort() {
    return this.request.getPort();
  }
  
  public String getQueryString() {
    return this.request.getQueryString();
  }
  
  public int getRemotePort() {
    return this.request.getPort();
  }
  
  public String getRequestURI() {
    return getPath();
  }
  
  public String getRequestURL() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(this.request.getScheme());
    stringBuilder.append("://");
    stringBuilder.append(this.request.getHost());
    if (this.request.getPort() != -1)
      stringBuilder.append(":")
        .append(this.request.getPort()); 
    stringBuilder.append(this.request.getPath());
    return stringBuilder.toString();
  }
  
  public String getScheme() {
    return this.request.getScheme();
  }
  
  public String getServerName() {
    return this.request.getHost();
  }
  
  public String getServletPath() {
    return this.request.getContextPath();
  }
  
  public String getURL() {
    return getRequestURL();
  }
  
  public boolean isMultipart() {
    return this.request.isMultipart();
  }
  
  public void setAttribute(String paramString, Object paramObject) {
    throw UnsupportedException;
  }
}
