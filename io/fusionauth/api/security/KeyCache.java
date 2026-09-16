package io.fusionauth.api.security;

import com.google.inject.Provider;
import com.inversoft.cache.CloseableReentrantReadWriteLock;
import com.inversoft.cache.SimpleCache;
import com.inversoft.util.Pair;
import io.fusionauth.api.service.system.KeyHelper;
import io.fusionauth.domain.Key;
import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.pem.domain.PEM;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class KeyCache extends SimpleCache<UUID, Key> implements Provider<Map<String, Verifier>> {
  private final Map<String, Key> jwtVerifierKeysByKid = new HashMap<>();
  
  private List<JSONWebKey> jsonWebKeys = new ArrayList<>();
  
  private Map<String, Verifier> verifiers = new HashMap<>();
  
  public Map<String, Verifier> get() {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      HashMap<String, Verifier> hashMap = new HashMap<>(this.verifiers);
      closeableLock.close();
      return hashMap;
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public PublicKey getPublicKey(UUID paramUUID) {
    Key key = (Key)get(paramUUID);
    if (key == null)
      return null; 
    if (key.certificate != null)
      return (PEM.decode(key.certificate)).publicKey; 
    return (key.publicKey == null) ? null : (PEM.decode(key.publicKey)).publicKey;
  }
  
  public SAMLKeySelector getSAMLKeySelector(@Nonnull List<UUID> paramList) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      Map<String, PublicKey> map = (Map)paramList.stream().map(this::get).filter(Objects::nonNull).map(paramKey -> paramKey.certificate).map(this::decodeCert).flatMap(Optional::stream).collect(Collectors.toMap(paramPair -> (String)paramPair.first, paramPair -> (PublicKey)paramPair.second, (paramPublicKey1, paramPublicKey2) -> paramPublicKey2));
      PublicKey publicKey = paramList.stream().findFirst().map(this::getPublicKey).orElse(null);
      SAMLKeySelector sAMLKeySelector = new SAMLKeySelector(map, publicKey);
      closeableLock.close();
      return sAMLKeySelector;
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public Verifier getVerifierByKeyId(UUID paramUUID) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      Key key = (Key)get(paramUUID);
      if (key == null) {
        Verifier verifier1 = null;
        closeableLock.close();
        return verifier1;
      } 
      Verifier verifier = KeyHelper.getVerifier(key);
      closeableLock.close();
      return verifier;
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public Key getVerifierKeyByKid(String paramString) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      Key key = this.jwtVerifierKeysByKid.get(paramString);
      closeableLock.close();
      return key;
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public Map<String, Verifier> getVerifiers() {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      HashMap<String, Verifier> hashMap = new HashMap<>(this.verifiers);
      closeableLock.close();
      return hashMap;
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public Map<String, Verifier> getVerifiersByKeyIds(@Nonnull List<UUID> paramList) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      Map<String, Verifier> map = (Map)paramList.stream().map(this::get).map(paramKey -> Pair.p(paramKey.kid, KeyHelper.getVerifier(paramKey))).filter(paramPair -> Objects.nonNull(paramPair.second)).collect(Collectors.toMap(paramPair -> (String)paramPair.first, paramPair -> (Verifier)paramPair.second));
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
  
  public List<JSONWebKey> getWebKeys() {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = new CloseableReentrantReadWriteLock.CloseableLock(this.lock.readLock());
    try {
      List<JSONWebKey> list = this.jsonWebKeys;
      closeableLock.close();
      return list;
    } catch (Throwable throwable) {
      try {
        closeableLock.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public void replace(Map<UUID, Key> paramMap) {
    throw new IllegalStateException("This method should not be called!");
  }
  
  public void replaceAll(Map<UUID, Key> paramMap, Map<String, Verifier> paramMap1) {
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = this.lock.openWrite();
    try {
      super.replace(paramMap);
      this.verifiers = new HashMap<>(paramMap1);
      this.jwtVerifierKeysByKid.clear();
      ArrayList<JSONWebKey> arrayList = new ArrayList();
      ArrayList arrayList1 = new ArrayList(paramMap.values());
      arrayList1.sort(Comparator.comparing(paramKey -> paramKey.insertInstant));
      for (Key key : arrayList1) {
        if (this.verifiers.containsKey(key.kid))
          this.jwtVerifierKeysByKid.put(key.kid, key); 
        if (key.type == Key.KeyType.HMAC || key.isExpired() || !key.isPair())
          continue; 
        JSONWebKey jSONWebKey = buildWebKey(key);
        arrayList.add(jSONWebKey);
      } 
      this.jsonWebKeys = arrayList;
      if (closeableLock != null)
        closeableLock.close(); 
    } catch (Throwable throwable) {
      if (closeableLock != null)
        try {
          closeableLock.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  JSONWebKey buildWebKey(Key paramKey) {
    JSONWebKey jSONWebKey = (paramKey.certificate != null) ? JSONWebKey.build(paramKey.certificate) : JSONWebKey.build(paramKey.publicKey);
    jSONWebKey.kid = paramKey.kid;
    if (paramKey.algorithm != null)
      jSONWebKey.alg = Algorithm.valueOf(paramKey.algorithm.name()); 
    return jSONWebKey;
  }
  
  private Optional<Pair<String, PublicKey>> decodeCert(String paramString) {
    try {
      PEM pEM = PEM.decode(paramString);
      String str = JWTUtils.generateJWS_x5t(pEM.certificate.getEncoded());
      return Optional.of(Pair.p(str, pEM.getPublicKey()));
    } catch (CertificateEncodingException|RuntimeException certificateEncodingException) {
      return Optional.empty();
    } 
  }
}
