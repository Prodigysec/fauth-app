package io.fusionauth.domain;

import io.fusionauth.domain.util.Normalizer;
import java.util.HashMap;

public class HTTPHeaders extends HashMap<String, String> {
  public HTTPHeaders() {}
  
  public HTTPHeaders(String paramString1, String paramString2) {
    put(paramString1, paramString2);
  }
  
  public HTTPHeaders(String paramString1, String paramString2, String paramString3, String paramString4) {
    put(paramString1, paramString2);
    put(paramString3, paramString4);
  }
  
  public HTTPHeaders(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6) {
    put(paramString1, paramString2);
    put(paramString3, paramString4);
    put(paramString5, paramString6);
  }
  
  public void normalize() {
    Normalizer.trimMap(this);
  }
}
