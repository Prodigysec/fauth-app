package io.fusionauth.api.service.cache;

import com.inversoft.cache.CloseableReentrantReadWriteLock;
import com.inversoft.cache.SimpleCache;
import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.ec.ECVerifier;
import io.fusionauth.jwt.rsa.RSAVerifier;
import java.net.URI;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONWebKeysCache extends SimpleCache<URI, List<JSONWebKey>> {
  private final Map<URI, Map<String, Verifier>> verifiers = new HashMap<>();
  
  public Map<String, Verifier> getVerifiers(URI paramURI) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      Map<String, Verifier> map = this.verifiers.get(paramURI);
      closeableLock.close();
      return map;
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public void replace(Map<URI, List<JSONWebKey>> paramMap) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.writeLock());
    try {
      this.cache.clear();
      this.cache.putAll(paramMap);
      this.verifiers.clear();
      paramMap.forEach((paramURI, paramList) -> this.verifiers.put(paramURI, buildVerifiers(paramList)));
      closeableLock.close();
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  private Map<String, Verifier> buildVerifiers(List<JSONWebKey> paramList) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    for (JSONWebKey jSONWebKey : paramList) {
      PublicKey publicKey = JSONWebKey.parse(jSONWebKey);
      if (publicKey instanceof RSAPublicKey) {
        RSAPublicKey rSAPublicKey = (RSAPublicKey)publicKey;
        hashMap.put(jSONWebKey.kid, RSAVerifier.newVerifier(rSAPublicKey));
        continue;
      } 
      if (publicKey instanceof ECPublicKey) {
        ECPublicKey eCPublicKey = (ECPublicKey)publicKey;
        hashMap.put(jSONWebKey.kid, ECVerifier.newVerifier(eCPublicKey));
      } 
    } 
    return (Map)hashMap;
  }
}
