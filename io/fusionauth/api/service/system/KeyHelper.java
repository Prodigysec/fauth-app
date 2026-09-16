package io.fusionauth.api.service.system;

import io.fusionauth.api.security.SecurityTools;
import io.fusionauth.domain.Key;
import io.fusionauth.jwt.HexUtils;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.ec.ECVerifier;
import io.fusionauth.jwt.ed.EdDSAVerifier;
import io.fusionauth.jwt.hmac.HMACVerifier;
import io.fusionauth.jwt.rsa.RSAVerifier;
import io.fusionauth.pem.domain.PEM;
import io.fusionauth.security.KeyUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class KeyHelper {
  public static Verifier getVerifier(Key paramKey) {
    switch (paramKey.type) {
      case EC:
        return 
          (paramKey.publicKey == null) ? null : (Verifier)ECVerifier.newVerifier(paramKey.publicKey);
      case HMAC:
        return (paramKey.secret == null) ? null : (Verifier)HMACVerifier.newVerifier(paramKey.secret);
      case RSA:
        return (paramKey.publicKey == null || KeyUtils.getKeyLength(PEM.decode(paramKey.publicKey).getPublicKey()) < 2048) ? 
          null : 
          
          (Verifier)RSAVerifier.newVerifier(paramKey.publicKey);
      case OKP:
        return (paramKey.publicKey == null) ? null : (Verifier)EdDSAVerifier.newVerifier((PEM.decode(paramKey.publicKey)).publicKey);
    } 
    return null;
  }
  
  public static List<UUID> normalizeVerificationKeyIds(UUID paramUUID1, List<UUID> paramList1, UUID paramUUID2, List<UUID> paramList2) {
    if (Objects.equals(paramList1, paramList2) && !Objects.equals(paramUUID1, paramUUID2)) {
      ArrayList<UUID> arrayList = new ArrayList<>(paramList1);
      arrayList.remove(paramUUID2);
      arrayList.remove(paramUUID1);
      if (paramUUID1 != null)
        arrayList.addFirst(paramUUID1); 
      return arrayList;
    } 
    return normalizeVerificationKeyIds(paramUUID1, paramList1);
  }
  
  public static List<UUID> normalizeVerificationKeyIds(UUID paramUUID, List<UUID> paramList) {
    ArrayList<UUID> arrayList = new ArrayList<>(paramList);
    if (paramUUID != null && arrayList.isEmpty())
      arrayList.add(paramUUID); 
    return arrayList;
  }
  
  public static void setupSyntheticFields(Key paramKey) {
    if (paramKey.publicKey == null && paramKey.certificate == null && paramKey.privateKey == null)
      return; 
    if (paramKey.publicKey == null && paramKey.certificate == null) {
      PEM pEM = PEM.decode(paramKey.privateKey);
      paramKey.length = Integer.valueOf(KeyUtils.getKeyLength(pEM.privateKey));
      setAlgorithm(paramKey);
    } else {
      PEM pEM = PEM.decode((paramKey.certificate != null) ? paramKey.certificate : paramKey.publicKey);
      paramKey.length = Integer.valueOf(KeyUtils.getKeyLength(pEM.publicKey));
      if (pEM.certificate instanceof X509Certificate)
        paramKey.certificateInformation = buildCertificateInformation(paramKey, (X509Certificate)pEM.certificate); 
      if (paramKey.privateKey != null)
        setAlgorithm(paramKey); 
      if (paramKey.privateKey != null && pEM.certificate == null) {
        X509Certificate x509Certificate = SecurityTools.generateX509Certificate(paramKey);
        paramKey.certificate = PEM.encode(x509Certificate);
        paramKey.certificateInformation = buildCertificateInformation(paramKey, x509Certificate);
      } 
    } 
    if (paramKey.type != Key.KeyType.HMAC)
      paramKey.hasPrivateKey = Boolean.valueOf((paramKey.privateKey != null)); 
  }
  
  private static Key.CertificateInformation buildCertificateInformation(Key paramKey, X509Certificate paramX509Certificate) {
    Key.CertificateInformation certificateInformation = new Key.CertificateInformation();
    certificateInformation.serialNumber = toNiceHexString(HexUtils.fromBytes(paramX509Certificate.getSerialNumber().toByteArray()));
    byte[] arrayOfByte = derEncodedBytes(paramX509Certificate);
    certificateInformation.sha1Thumbprint = digest("SHA-1", arrayOfByte);
    certificateInformation.sha256Thumbprint = digest("SHA-256", arrayOfByte);
    certificateInformation.sha1Fingerprint = toNiceHexString(thumbprintToFingerprint(certificateInformation.sha1Thumbprint));
    certificateInformation.md5Fingerprint = md5Fingerprint(arrayOfByte);
    certificateInformation.sha256Fingerprint = toNiceHexString(thumbprintToFingerprint(certificateInformation.sha256Thumbprint));
    certificateInformation.validFrom = ZonedDateTime.ofInstant(paramX509Certificate.getNotBefore().toInstant(), ZoneOffset.UTC);
    certificateInformation.validTo = ZonedDateTime.ofInstant(paramX509Certificate.getNotAfter().toInstant(), ZoneOffset.UTC);
    paramKey.expirationInstant = certificateInformation.validTo;
    certificateInformation.issuer = paramX509Certificate.getIssuerDN().getName();
    certificateInformation.subject = paramX509Certificate.getSubjectDN().getName();
    return certificateInformation;
  }
  
  private static byte[] derEncodedBytes(X509Certificate paramX509Certificate) {
    try {
      return paramX509Certificate.getEncoded();
    } catch (CertificateEncodingException certificateEncodingException) {
      throw new IllegalStateException("Some certificate was loaded that wasn't a X.509. This shouldn't be possible on the JDK. The certificate was [" + String.valueOf(paramX509Certificate) + "]", certificateEncodingException);
    } 
  }
  
  private static String digest(String paramString, byte[] paramArrayOfbyte) {
    byte[] arrayOfByte = rawDigest(paramString, paramArrayOfbyte);
    return new String(Base64.getUrlEncoder().withoutPadding().encode(arrayOfByte));
  }
  
  private static String md5Fingerprint(byte[] paramArrayOfbyte) {
    return toNiceHexString(HexUtils.fromBytes(rawDigest("MD5", paramArrayOfbyte)));
  }
  
  private static byte[] rawDigest(String paramString, byte[] paramArrayOfbyte) {
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance(paramString);
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new IllegalArgumentException("No such algorithm [" + paramString + "]");
    } 
    return messageDigest.digest(paramArrayOfbyte);
  }
  
  private static void setAlgorithm(Key paramKey) {
    if (paramKey.type == Key.KeyType.EC) {
      if (paramKey.length.intValue() == 256) {
        paramKey.algorithm = Key.KeyAlgorithm.ES256;
      } else if (paramKey.length.intValue() == 384) {
        paramKey.algorithm = Key.KeyAlgorithm.ES384;
      } else {
        paramKey.algorithm = Key.KeyAlgorithm.ES512;
      } 
    } else if (paramKey.type == Key.KeyType.OKP) {
      paramKey.algorithm = Key.KeyAlgorithm.Ed25519;
    } 
  }
  
  private static String thumbprintToFingerprint(String paramString) {
    byte[] arrayOfByte = Base64.getUrlDecoder().decode(paramString.getBytes(StandardCharsets.UTF_8));
    return HexUtils.fromBytes(arrayOfByte);
  }
  
  private static String toNiceHexString(String paramString) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < paramString.length(); i += 2) {
      stringBuilder.append(paramString.charAt(i)).append(paramString.charAt(i + 1));
      if (i + 2 < paramString.length())
        stringBuilder.append(":"); 
    } 
    return stringBuilder.toString();
  }
}
