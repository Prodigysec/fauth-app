package io.fusionauth.api.service.system;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DefaultCipherService implements CipherService {
  private final ObjectMapper objectMapper;
  
  private final SystemConfigurationCache systemConfigurationCache;
  
  @Inject
  public DefaultCipherService(ObjectMapper paramObjectMapper, SystemConfigurationCache paramSystemConfigurationCache) {
    this.objectMapper = paramObjectMapper;
    this.systemConfigurationCache = paramSystemConfigurationCache;
  }
  
  public <T> T decrypt(String paramString, Class<T> paramClass) throws GeneralSecurityException, IOException {
    byte[] arrayOfByte = decrypt(paramString);
    return (T)this.objectMapper.readerFor(paramClass).readValue(arrayOfByte);
  }
  
  public byte[] decrypt(String paramString, byte[] paramArrayOfbyte) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
    byte[] arrayOfByte1 = Arrays.copyOfRange(paramArrayOfbyte, 0, 16);
    byte[] arrayOfByte2 = Arrays.copyOfRange(paramArrayOfbyte, 16, paramArrayOfbyte.length);
    try {
      Cipher cipher = getGCMCipher(arrayOfByte1, paramString, 2);
      return decryptBytes(arrayOfByte2, cipher);
    } catch (ShortBufferException|IllegalBlockSizeException|BadPaddingException shortBufferException) {
      try {
        Cipher cipher = getCBCCipher(arrayOfByte1, paramString, 2);
        return decryptBytes(arrayOfByte2, cipher);
      } catch (ShortBufferException|IllegalBlockSizeException|BadPaddingException shortBufferException1) {
        throw shortBufferException;
      } 
    } 
  }
  
  public byte[] decrypt(byte[] paramArrayOfbyte) throws ShortBufferException, BadPaddingException, IllegalBlockSizeException {
    return decrypt((this.systemConfigurationCache.get()).cookieEncryptionKey, paramArrayOfbyte);
  }
  
  public String encrypt(Object paramObject) throws GeneralSecurityException, JsonProcessingException {
    byte[] arrayOfByte1 = this.objectMapper.writeValueAsBytes(paramObject);
    byte[] arrayOfByte2 = encrypt(arrayOfByte1);
    return Base64.getEncoder().encodeToString(arrayOfByte2);
  }
  
  public byte[] encrypt(byte[] paramArrayOfbyte) throws GeneralSecurityException {
    String str = (this.systemConfigurationCache.get()).cookieEncryptionKey;
    return encryptBytes(str, paramArrayOfbyte);
  }
  
  public String encrypt(String paramString, Object paramObject) throws JsonProcessingException, GeneralSecurityException {
    byte[] arrayOfByte = this.objectMapper.writeValueAsBytes(paramObject);
    return encryptBytesAndEncode(paramString, arrayOfByte);
  }
  
  public String encrypt(String paramString, byte[] paramArrayOfbyte) throws GeneralSecurityException {
    return encryptBytesAndEncode(paramString, paramArrayOfbyte);
  }
  
  private byte[] decrypt(String paramString) throws ShortBufferException, BadPaddingException, IllegalBlockSizeException {
    byte[] arrayOfByte = Base64.getDecoder().decode(paramString.getBytes(StandardCharsets.UTF_8));
    return decrypt(arrayOfByte);
  }
  
  private byte[] decryptBytes(byte[] paramArrayOfbyte, Cipher paramCipher) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
    byte[] arrayOfByte = new byte[paramCipher.getOutputSize(paramArrayOfbyte.length)];
    int i = paramCipher.update(paramArrayOfbyte, 0, paramArrayOfbyte.length, arrayOfByte, 0);
    i += paramCipher.doFinal(arrayOfByte, i);
    return Arrays.copyOfRange(arrayOfByte, 0, i);
  }
  
  private byte[] encryptBytes(byte[] paramArrayOfbyte, Cipher paramCipher) throws GeneralSecurityException {
    byte[] arrayOfByte1 = new byte[paramCipher.getOutputSize(paramArrayOfbyte.length)];
    int i = paramCipher.update(paramArrayOfbyte, 0, paramArrayOfbyte.length, arrayOfByte1, 0);
    i += paramCipher.doFinal(arrayOfByte1, i);
    byte[] arrayOfByte2 = paramCipher.getIV();
    byte[] arrayOfByte3 = new byte[arrayOfByte2.length + i];
    System.arraycopy(arrayOfByte2, 0, arrayOfByte3, 0, arrayOfByte2.length);
    System.arraycopy(arrayOfByte1, 0, arrayOfByte3, arrayOfByte2.length, i);
    return arrayOfByte3;
  }
  
  private byte[] encryptBytes(String paramString, byte[] paramArrayOfbyte) throws GeneralSecurityException {
    byte[] arrayOfByte = new byte[16];
    (new SecureRandom()).nextBytes(arrayOfByte);
    Cipher cipher = getGCMCipher(arrayOfByte, paramString, 1);
    return encryptBytes(paramArrayOfbyte, cipher);
  }
  
  private String encryptBytesAndEncode(String paramString, byte[] paramArrayOfbyte) throws GeneralSecurityException {
    return Base64.getEncoder().encodeToString(encryptBytes(paramString, paramArrayOfbyte));
  }
  
  private Cipher getCBCCipher(byte[] paramArrayOfbyte, String paramString, int paramInt) {
    try {
      IvParameterSpec ivParameterSpec = new IvParameterSpec(paramArrayOfbyte);
      byte[] arrayOfByte = Base64.getDecoder().decode(paramString.getBytes(StandardCharsets.UTF_8));
      SecretKeySpec secretKeySpec = new SecretKeySpec(arrayOfByte, "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(paramInt, secretKeySpec, ivParameterSpec);
      return cipher;
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    } 
  }
  
  private Cipher getGCMCipher(byte[] paramArrayOfbyte, String paramString, int paramInt) {
    try {
      byte[] arrayOfByte = Base64.getDecoder().decode(paramString.getBytes(StandardCharsets.UTF_8));
      SecretKeySpec secretKeySpec = new SecretKeySpec(arrayOfByte, "AES");
      GCMParameterSpec gCMParameterSpec = new GCMParameterSpec(128, paramArrayOfbyte);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(paramInt, secretKeySpec, gCMParameterSpec);
      return cipher;
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    } 
  }
}
