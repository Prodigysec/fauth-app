package io.fusionauth.api.security;

import io.fusionauth.domain.Key;
import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.jwt.hmac.HMACSigner;
import io.fusionauth.pem.domain.PEM;
import io.fusionauth.security.KeyUtils;
import java.math.BigInteger;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class SecurityTools {
  public static X509Certificate generateX509Certificate(Key paramKey) throws IllegalArgumentException {
    try {
      X509CertInfo x509CertInfo = new X509CertInfo();
      PublicKey publicKey = (PEM.decode(paramKey.publicKey)).publicKey;
      CertificateX509Key certificateX509Key = new CertificateX509Key(publicKey);
      x509CertInfo.setKey(certificateX509Key);
      x509CertInfo.setVersion(new CertificateVersion(1));
      x509CertInfo.setAlgorithmId(new CertificateAlgorithmId(new AlgorithmId(toObjectIdentifierOld(paramKey.algorithm))));
      x509CertInfo.setIssuer(new X500Name("CN=" + paramKey.issuer));
      x509CertInfo.setSubject(new X500Name("CN=" + paramKey.issuer));
      x509CertInfo.setValidity(new CertificateValidity(Date.from(paramKey.insertInstant.toInstant()), Date.from(paramKey.insertInstant.plusYears(10L).toInstant())));
      x509CertInfo.setSerialNumber(new CertificateSerialNumber(new BigInteger(paramKey.id.toString().replace("-", ""), 16)));
      PrivateKey privateKey = (PEM.decode(paramKey.privateKey)).privateKey;
      return X509CertImpl.newSigned(x509CertInfo, privateKey, paramKey.algorithm.algorithm);
    } catch (Exception exception) {
      throw new IllegalArgumentException(exception);
    } 
  }
  
  public static boolean isECDSA(String paramString) {
    try {
      PEM pEM = PEM.decode(paramString);
      if (pEM.privateKey != null)
        return pEM.privateKey instanceof java.security.interfaces.ECPrivateKey; 
      if (pEM.publicKey != null)
        return pEM.publicKey instanceof java.security.interfaces.ECPublicKey; 
    } catch (Exception exception) {}
    return false;
  }
  
  public static boolean isECDSA(Key.KeyAlgorithm paramKeyAlgorithm) {
    return (keyTypeFromAlgorithm(paramKeyAlgorithm) == Key.KeyType.EC);
  }
  
  public static boolean isEdDSA(String paramString) {
    try {
      PEM pEM = PEM.decode(paramString);
      if (pEM.privateKey != null)
        return pEM.privateKey instanceof java.security.interfaces.EdECPrivateKey; 
      if (pEM.publicKey != null)
        return pEM.publicKey instanceof java.security.interfaces.EdECPublicKey; 
    } catch (Exception exception) {}
    return false;
  }
  
  public static boolean isEdDSA(Key.KeyAlgorithm paramKeyAlgorithm) {
    return (keyTypeFromAlgorithm(paramKeyAlgorithm) == Key.KeyType.OKP);
  }
  
  public static boolean isHMAC(Key.KeyAlgorithm paramKeyAlgorithm) {
    return (keyTypeFromAlgorithm(paramKeyAlgorithm) == Key.KeyType.HMAC);
  }
  
  public static boolean isPEMEncoded(String paramString) {
    try {
      PEM.decode(paramString);
      return true;
    } catch (Exception exception) {
      return false;
    } 
  }
  
  public static boolean isRSA(Key.KeyAlgorithm paramKeyAlgorithm) {
    return (keyTypeFromAlgorithm(paramKeyAlgorithm) == Key.KeyType.RSA);
  }
  
  public static boolean isRSA(String paramString) {
    try {
      PEM pEM = PEM.decode(paramString);
      if (pEM.privateKey != null)
        return pEM.privateKey instanceof RSAPrivateKey; 
      if (pEM.publicKey != null)
        return pEM.publicKey instanceof RSAPublicKey; 
    } catch (Exception exception) {}
    return false;
  }
  
  public static Algorithm jwtAlgorithmFromKeyAlgorithm(Key.KeyAlgorithm paramKeyAlgorithm) {
    switch (paramKeyAlgorithm) {
      default:
        throw new MatchException(null, null);
      case Ed25519:
      
      case ES256:
      
      case ES384:
      
      case ES512:
      
      case HS256:
      
      case HS384:
      
      case HS512:
      
      case RS256:
      
      case RS384:
      
      case RS512:
      
      case None:
        break;
    } 
    return 









      
      Algorithm.none;
  }
  
  public static Key.KeyType keyTypeFromAlgorithm(Key.KeyAlgorithm paramKeyAlgorithm) {
    // Byte code:
    //   0: aload_0
    //   1: astore_1
    //   2: iconst_0
    //   3: istore_2
    //   4: aload_1
    //   5: iload_2
    //   6: <illegal opcode> enumSwitch : (Lio/fusionauth/domain/Key$KeyAlgorithm;I)I
    //   11: tableswitch default -> 72, -1 -> 110, 0 -> 82, 1 -> 82, 2 -> 82, 3 -> 88, 4 -> 88, 5 -> 88, 6 -> 94, 7 -> 94, 8 -> 94, 9 -> 100, 10 -> 106
    //   72: new java/lang/MatchException
    //   75: dup
    //   76: aconst_null
    //   77: aconst_null
    //   78: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   81: athrow
    //   82: getstatic io/fusionauth/domain/Key$KeyType.RSA : Lio/fusionauth/domain/Key$KeyType;
    //   85: goto -> 111
    //   88: getstatic io/fusionauth/domain/Key$KeyType.HMAC : Lio/fusionauth/domain/Key$KeyType;
    //   91: goto -> 111
    //   94: getstatic io/fusionauth/domain/Key$KeyType.EC : Lio/fusionauth/domain/Key$KeyType;
    //   97: goto -> 111
    //   100: getstatic io/fusionauth/domain/Key$KeyType.OKP : Lio/fusionauth/domain/Key$KeyType;
    //   103: goto -> 111
    //   106: aconst_null
    //   107: goto -> 111
    //   110: aconst_null
    //   111: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #158	-> 0
    //   #159	-> 82
    //   #160	-> 88
    //   #161	-> 94
    //   #162	-> 100
    //   #163	-> 106
    //   #164	-> 110
    //   #158	-> 111
  }
  
  public static Key.KeyType keyTypeFromPEM(String paramString) {
    if (isRSA(paramString))
      return Key.KeyType.RSA; 
    if (isECDSA(paramString))
      return Key.KeyType.EC; 
    return Key.KeyType.OKP;
  }
  
  public static String lastN(String paramString, int paramInt) {
    if (paramString.length() > paramInt)
      return "*" + paramString.substring(paramString.length() - paramInt); 
    return paramString;
  }
  
  public static boolean validPrivateKey(String paramString) {
    return validPrivateKey(paramString, null);
  }
  
  public static boolean validPrivateKey(String paramString, Key.KeyAlgorithm paramKeyAlgorithm) {
    try {
      PrivateKey privateKey = PEM.decode(paramString).getPrivateKey();
      if (privateKey instanceof RSAPrivateKey)
        return (((RSAPrivateKey)privateKey).getModulus().bitLength() >= 2048); 
      if (privateKey instanceof java.security.interfaces.ECPrivateKey && paramKeyAlgorithm != null)
        return doesECKeyLengthMatchAlgorithm(privateKey, paramKeyAlgorithm); 
      return true;
    } catch (Exception exception) {
      return false;
    } 
  }
  
  public static boolean validPublicKey(String paramString) {
    return validPublicKey(paramString, null);
  }
  
  public static boolean validPublicKey(String paramString, Key.KeyAlgorithm paramKeyAlgorithm) {
    return validPublicKey(paramString, paramKeyAlgorithm, 1024);
  }
  
  public static boolean validPublicKeyInKeyPair(String paramString, Key.KeyAlgorithm paramKeyAlgorithm) {
    return validPublicKey(paramString, paramKeyAlgorithm, 2048);
  }
  
  public static boolean validSecret(String paramString) {
    try {
      HMACSigner.newSHA256Signer(paramString).sign("test");
      return true;
    } catch (Exception exception) {
      return false;
    } 
  }
  
  private static byte[] bytes(int... paramVarArgs) {
    byte[] arrayOfByte = new byte[paramVarArgs.length];
    for (byte b = 0; b < paramVarArgs.length; b++)
      arrayOfByte[b] = (byte)paramVarArgs[b]; 
    return arrayOfByte;
  }
  
  private static boolean doesECKeyLengthMatchAlgorithm(Key paramKey, Key.KeyAlgorithm paramKeyAlgorithm) {
    int i = KeyUtils.getKeyLength(paramKey);
    switch (paramKeyAlgorithm) {
      case ES256:
        return 
          (i == 256);
      case ES384:
        return (i == 384);
      case ES512:
        return (i == 521);
    } 
    return false;
  }
  
  private static boolean doesEdDSAKeyLengthMatchAlgorithm(Key paramKey, Key.KeyAlgorithm paramKeyAlgorithm) {
    int i = KeyUtils.getKeyLength(paramKey);
    return (paramKeyAlgorithm == Key.KeyAlgorithm.Ed25519 && i == 32);
  }
  
  private static byte[] toObjectIdentifier(Key.KeyAlgorithm paramKeyAlgorithm) {
    switch (paramKeyAlgorithm) {
      case ES256:
        return new byte[] { 6, 8, 42, -122, 72, -50, 61, 4, 3, 2 };
      case ES384:
        return new byte[] { 6, 8, 42, -122, 72, -50, 61, 4, 3, 3 };
      case ES512:
        return new byte[] { 6, 8, 42, -122, 72, -50, 61, 4, 3, 4 };
      case RS256:
        return bytes(new int[] { 42, 134, 72, 134, 247, 13, 1, 1, 11 });
      case RS384:
        return bytes(new int[] { 42, 134, 72, 134, 247, 13, 1, 1, 12 });
      case RS512:
        return bytes(new int[] { 42, 134, 72, 134, 247, 13, 1, 1, 13 });
      case Ed25519:
        return bytes(new int[] { 6, 3, 43, 101, 112 });
    } 
    throw new IllegalArgumentException("Invalid KeyMaster key to generate a certificate for");
  }
  
  private static ObjectIdentifier toObjectIdentifierOld(Key.KeyAlgorithm paramKeyAlgorithm) {
    switch (paramKeyAlgorithm) {
      case ES256:
        return ObjectIdentifier.of(KnownOIDs.SHA256withECDSA);
      case ES384:
        return ObjectIdentifier.of(KnownOIDs.SHA384withECDSA);
      case ES512:
        return ObjectIdentifier.of(KnownOIDs.SHA512withECDSA);
      case RS256:
        return ObjectIdentifier.of(KnownOIDs.SHA256withRSA);
      case RS384:
        return ObjectIdentifier.of(KnownOIDs.SHA384withRSA);
      case RS512:
        return ObjectIdentifier.of(KnownOIDs.SHA512withRSA);
      case Ed25519:
        return ObjectIdentifier.of(KnownOIDs.Ed25519);
    } 
    throw new IllegalArgumentException("Invalid KeyMaster key to generate a certificate for");
  }
  
  private static boolean validPublicKey(String paramString, Key.KeyAlgorithm paramKeyAlgorithm, int paramInt) {
    try {
      PublicKey publicKey = PEM.decode(paramString).getPublicKey();
      if (publicKey instanceof RSAPublicKey)
        return (((RSAPublicKey)publicKey).getModulus().bitLength() >= paramInt); 
      if (publicKey instanceof java.security.interfaces.ECPublicKey && paramKeyAlgorithm != null)
        return doesECKeyLengthMatchAlgorithm(publicKey, paramKeyAlgorithm); 
      if (publicKey instanceof java.security.interfaces.EdECPublicKey && paramKeyAlgorithm != null)
        return doesEdDSAKeyLengthMatchAlgorithm(publicKey, paramKeyAlgorithm); 
      return true;
    } catch (Exception exception) {
      return false;
    } 
  }
}
