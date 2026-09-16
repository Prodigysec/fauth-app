package io.fusionauth.api.util;

import com.google.inject.Inject;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.system.KeyService;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.KeyUse;
import java.util.UUID;

public class KeyValidator {
  private final KeyReaderService keyReader;
  
  @Inject
  public KeyValidator(KeyReaderService paramKeyReaderService) {
    this.keyReader = paramKeyReaderService;
  }
  
  public static boolean validCertificate(Key paramKey) {
    return (paramKey.certificate != null && paramKey.certificateInformation != null);
  }
  
  public static boolean validForAccessTokenSigning(Key paramKey) {
    return (paramKey.use() == KeyUse.SignAndVerify && !KeyService.ClientSecretShadowKeys.contains(paramKey.id));
  }
  
  public static boolean validForAccessTokenVerification(Key paramKey) {
    return (paramKey.canVerify() && !KeyService.ClientSecretShadowKeys.contains(paramKey.id));
  }
  
  public static boolean validForAppleSigning(Key paramKey) {
    return (paramKey.canSign() && !KeyService.ClientSecretShadowKeys.contains(paramKey.id));
  }
  
  public static boolean validForExternalJwtVerification(Key paramKey) {
    return (paramKey.canVerify() && !KeyService.ClientSecretShadowKeys.contains(paramKey.id));
  }
  
  public static boolean validForIdTokenSigning(Key paramKey) {
    return (paramKey.use() == KeyUse.SignAndVerify);
  }
  
  public static boolean validForIdTokenVerification(Key paramKey) {
    return paramKey.canVerify();
  }
  
  public static boolean validForSAMLDecryption(Key paramKey) {
    return (paramKey.type == Key.KeyType.RSA && paramKey.canSign());
  }
  
  public static boolean validForSAMLEncryption(Key paramKey) {
    return (paramKey.type == Key.KeyType.RSA && paramKey.canVerify());
  }
  
  public static boolean validForSAMLSigning(Key paramKey) {
    boolean bool = (paramKey.type == Key.KeyType.RSA || paramKey.type == Key.KeyType.EC) ? true : false;
    return (bool && paramKey.use() == KeyUse.SignAndVerify && paramKey.certificateInformation != null);
  }
  
  public static boolean validForSAMLVerification(Key paramKey) {
    return (paramKey.type != Key.KeyType.HMAC && paramKey.type != Key.KeyType.OKP && paramKey.canVerify());
  }
  
  public static boolean validForWebhookSigning(Key paramKey) {
    return (paramKey.canSign() && !KeyService.ClientSecretShadowKeys.contains(paramKey.id));
  }
  
  private static String invalidCodeFor(String paramString) {
    int i = paramString.indexOf(']');
    return (i > 0) ? ("[invalid]" + paramString.substring(i + 1)) : paramString;
  }
  
  public void validateAccessTokenSigningKey(Validator paramValidator, UUID paramUUID, String paramString) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObject(key, paramString, new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensure((key.use() == KeyUse.SignAndVerify), paramString, "[cannotSign]", new Object[] { paramUUID }); 
    paramValidator.ifLastCheckHadNoError(() -> paramValidator.ensure(!KeyService.ClientSecretShadowKeys.contains(paramUUID), paramString, "[onlyIdToken]", new Object[] { paramUUID }));
  }
  
  public void validateAccessTokenVerificationKey(Validator paramValidator, UUID paramUUID, String paramString1, String paramString2) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObjectWithCode(key, paramString1, invalidCodeFor(paramString2), new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensureWithCode(validForAccessTokenVerification(key), paramString1, paramString2, new Object[] { paramUUID }); 
  }
  
  public void validateAppleSigningKey(Validator paramValidator, UUID paramUUID, String paramString1, String paramString2, String paramString3) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObjectWithCode(key, paramString1, paramString2, new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensureWithCode(validForAppleSigning(key), paramString1, paramString3, new Object[] { paramUUID }); 
  }
  
  public void validateCertificate(Validator paramValidator, UUID paramUUID, String paramString) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObject(key, paramString, new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensure(validCertificate(key), paramString, "[noCertificate]", new Object[] { paramUUID }); 
  }
  
  public void validateDecryptionKeyForSAML(Validator paramValidator, UUID paramUUID, String paramString) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObject(key, paramString, new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensure(validForSAMLDecryption(key), paramString, "[cannotDecrypt]", new Object[] { paramUUID }); 
  }
  
  public void validateEncryptionKeyForSAML(Validator paramValidator, UUID paramUUID, String paramString) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObject(key, paramString, new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensure(validForSAMLEncryption(key), paramString, "[cannotEncrypt]", new Object[] { paramUUID }); 
  }
  
  public void validateExternalJwtVerificationKey(Validator paramValidator, UUID paramUUID, String paramString) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObject(key, paramString, new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensure(validForExternalJwtVerification(key), paramString, "[cannotVerify]", new Object[] { paramUUID }); 
  }
  
  public void validateExternalJwtVerificationKey(Validator paramValidator, UUID paramUUID, String paramString1, String paramString2) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObjectWithCode(key, paramString1, invalidCodeFor(paramString2), new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensureWithCode(validForExternalJwtVerification(key), paramString1, paramString2, new Object[] { paramUUID }); 
  }
  
  public void validateIdTokenSigningKey(Validator paramValidator, UUID paramUUID, String paramString) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObject(key, paramString, new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensure(validForIdTokenSigning(key), paramString, "[cannotSign]", new Object[] { paramUUID }); 
  }
  
  public void validateIdTokenVerificationKey(Validator paramValidator, UUID paramUUID, String paramString1, String paramString2) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObjectWithCode(key, paramString1, invalidCodeFor(paramString2), new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensureWithCode(validForIdTokenVerification(key), paramString1, paramString2, new Object[] { paramUUID }); 
  }
  
  public void validateSigningKey(Validator paramValidator, UUID paramUUID, String paramString, boolean paramBoolean1, boolean paramBoolean2) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObject(key, paramString, new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensure((key.canSign() && (!paramBoolean1 || key.certificateInformation != null)), paramString, "[cannotSign]", new Object[] { paramUUID }); 
    if (paramBoolean2)
      paramValidator.ifLastCheckHadNoError(() -> paramValidator.ensure(!KeyService.ClientSecretShadowKeys.contains(paramUUID), paramString, "[onlyIdToken]", new Object[] { paramUUID })); 
  }
  
  public void validateSigningKeyForSAML(Validator paramValidator, UUID paramUUID, String paramString) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObject(key, paramString, new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensure(validForSAMLSigning(key), paramString, "[cannotSign]", new Object[] { paramUUID }); 
  }
  
  public void validateSigningKeyForWebhook(Validator paramValidator, UUID paramUUID, String paramString) {
    validateSigningKey(paramValidator, paramUUID, paramString, false, true);
  }
  
  public void validateVerifyKeyForSAML(Validator paramValidator, UUID paramUUID, String paramString) {
    validateVerifyKeyForSAML(paramValidator, paramUUID, paramString, "[cannotVerify]" + paramString);
  }
  
  public void validateVerifyKeyForSAML(Validator paramValidator, UUID paramUUID, String paramString1, String paramString2) {
    Key key = this.keyReader.retrieveById(paramUUID);
    paramValidator.validObjectWithCode(key, paramString1, invalidCodeFor(paramString2), new Object[] { paramUUID });
    if (key != null)
      paramValidator.ensureWithCode(validForSAMLVerification(key), paramString1, paramString2, new Object[] { paramUUID }); 
  }
}
