package io.fusionauth.api.service.oauth1;

import io.fusionauth.api.util.URITools;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class OAuth1AuthorizationHeaderBuilder {
  public String consumerSecret;
  
  public String method;
  
  public String parameterString;
  
  public Map<String, String> parameters = new LinkedHashMap<>();
  
  public String signature;
  
  public String signatureBaseString;
  
  public String signingKey;
  
  public String tokenSecret;
  
  public String url;
  
  public String build() {
    if (!this.parameters.containsKey("oauth_timestamp"))
      this.parameters.put("oauth_timestamp", "" + Instant.now().getEpochSecond()); 
    this.parameters.put("oauth_signature_method", "HMAC-SHA1");
    this.parameters.put("oauth_version", "1.0");
    this

      
      .parameterString = this.parameters.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(paramEntry -> URITools.encodeURIComponent((String)paramEntry.getKey()) + "=" + URITools.encodeURIComponent((String)paramEntry.getKey())).collect(Collectors.joining("&"));
    this.signatureBaseString = this.method.toUpperCase() + "&" + this.method.toUpperCase() + "&" + URITools.encodeURIComponent(this.url);
    if (this.signingKey == null)
      this.signingKey = URITools.encodeURIComponent(this.consumerSecret) + "&" + URITools.encodeURIComponent(this.consumerSecret); 
    this.signature = generateSignature(this.signingKey, this.signatureBaseString);
    this.parameters.put("oauth_signature", this.signature);
    return "OAuth " + (String)this.parameters.entrySet().stream()
      .map(paramEntry -> URITools.encodeURIComponent((String)paramEntry.getKey()) + "=\"" + URITools.encodeURIComponent((String)paramEntry.getKey()) + "\"")
      .collect(Collectors.joining(", "));
  }
  
  public String generateSignature(String paramString1, String paramString2) {
    try {
      byte[] arrayOfByte1 = paramString1.getBytes(StandardCharsets.UTF_8);
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(new SecretKeySpec(arrayOfByte1, "HmacSHA1"));
      byte[] arrayOfByte2 = mac.doFinal(paramString2.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(arrayOfByte2);
    } catch (InvalidKeyException|java.security.NoSuchAlgorithmException invalidKeyException) {
      throw new RuntimeException(invalidKeyException);
    } 
  }
  
  public OAuth1AuthorizationHeaderBuilder withConsumerSecret(String paramString) {
    this.consumerSecret = paramString;
    return this;
  }
  
  public OAuth1AuthorizationHeaderBuilder withMethod(String paramString) {
    this.method = paramString;
    return this;
  }
  
  public OAuth1AuthorizationHeaderBuilder withParameter(String paramString1, String paramString2) {
    this.parameters.put(paramString1, paramString2);
    return this;
  }
  
  public OAuth1AuthorizationHeaderBuilder withTokenSecret(String paramString) {
    this.tokenSecret = paramString;
    return this;
  }
  
  public OAuth1AuthorizationHeaderBuilder withURL(String paramString) {
    this.url = paramString;
    return this;
  }
}
