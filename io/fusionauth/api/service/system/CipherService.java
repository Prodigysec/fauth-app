package io.fusionauth.api.service.system;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

public interface CipherService {
  <T> T decrypt(String paramString, Class<T> paramClass) throws IOException, GeneralSecurityException;
  
  byte[] decrypt(String paramString, byte[] paramArrayOfbyte) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException;
  
  byte[] decrypt(byte[] paramArrayOfbyte) throws GeneralSecurityException, JsonProcessingException;
  
  byte[] encrypt(byte[] paramArrayOfbyte) throws GeneralSecurityException;
  
  String encrypt(Object paramObject) throws GeneralSecurityException, JsonProcessingException;
  
  String encrypt(String paramString, byte[] paramArrayOfbyte) throws GeneralSecurityException;
  
  String encrypt(String paramString, Object paramObject) throws JsonProcessingException, GeneralSecurityException;
}
