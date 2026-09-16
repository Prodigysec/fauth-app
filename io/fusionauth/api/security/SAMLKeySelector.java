package io.fusionauth.api.security;

import io.fusionauth.jwt.JWTUtils;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;

public class SAMLKeySelector extends KeySelector {
  private final PublicKey defaultKey;
  
  private final Map<String, PublicKey> keys = new HashMap<>();
  
  public SAMLKeySelector(Map<String, PublicKey> paramMap, PublicKey paramPublicKey) {
    this.defaultKey = paramPublicKey;
    this.keys.putAll(paramMap);
  }
  
  public KeySelectorResult select(KeyInfo paramKeyInfo, KeySelector.Purpose paramPurpose, AlgorithmMethod paramAlgorithmMethod, XMLCryptoContext paramXMLCryptoContext) {
    if (paramKeyInfo == null)
      return () -> this.defaultKey; 
    X509Data x509Data = paramKeyInfo.getContent().stream().filter(paramXMLStructure -> paramXMLStructure instanceof X509Data).findFirst().orElse(null);
    if (x509Data != null) {
      X509Certificate x509Certificate = x509Data.getContent().stream().filter(paramObject -> paramObject instanceof X509Certificate).findFirst().orElse(null);
      if (x509Certificate != null)
        try {
          String str = JWTUtils.generateJWS_x5t(x509Certificate.getEncoded());
          return () -> (Key)this.keys.get(paramString);
        } catch (CertificateEncodingException certificateEncodingException) {} 
    } 
    return () -> this.defaultKey;
  }
}
