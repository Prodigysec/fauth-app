package io.fusionauth.app.primeframework;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.inject.Inject;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.domain.SystemConfigurationMapper;
import io.fusionauth.api.domain.SystemEncryptionKey;
import io.fusionauth.domain.SystemConfiguration;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.security.Key;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.mybatis.guice.transactional.Transactional;
import org.primeframework.mvc.config.AbstractMVCConfiguration;
import org.primeframework.mvc.parameter.annotation.FieldUnwrapped;

public class FusionAuthMVCConfiguration extends AbstractMVCConfiguration {
  protected final Key cookieEncryptionKey;
  
  private final Path baseDirectory;
  
  private final FusionAuthConfiguration configuration;
  
  @Inject
  public FusionAuthMVCConfiguration(FusionAuthConfiguration paramFusionAuthConfiguration, SystemConfigurationMapper paramSystemConfigurationMapper) {
    SystemEncryptionKey systemEncryptionKey = buildEncryptionKey(paramSystemConfigurationMapper);
    this.baseDirectory = paramFusionAuthConfiguration.homeDirectory().resolve("web");
    this.cookieEncryptionKey = systemEncryptionKey.getKey();
    this.configuration = paramFusionAuthConfiguration;
    this.localeCookieName = "fusionauth.locale";
  }
  
  public boolean allowUnknownParameters() {
    return (this.configuration.runtimeMode() == RuntimeMode.Production || this.configuration.runtimeMode() == RuntimeMode.Development || this.configuration.runtimeMode() == RuntimeMode.FusionAuth_Demo);
  }
  
  public Path baseDirectory() {
    return this.baseDirectory;
  }
  
  @Transactional
  public SystemEncryptionKey buildEncryptionKey(SystemConfigurationMapper paramSystemConfigurationMapper) {
    SystemConfiguration systemConfiguration = paramSystemConfigurationMapper.retrieveAndLock();
    if (systemConfiguration.cookieEncryptionKey == null) {
      byte[] arrayOfByte = new byte[16];
      (new SecureRandom()).nextBytes(arrayOfByte);
      systemConfiguration.cookieEncryptionKey = Base64.getEncoder().encodeToString(arrayOfByte);
      paramSystemConfigurationMapper.update(systemConfiguration);
    } 
    return new SystemEncryptionKey(systemConfiguration.cookieEncryptionKey);
  }
  
  public int collectionSizeLimit() {
    return this.configuration.collectionSizeLimit();
  }
  
  public Key cookieEncryptionKey() {
    return this.cookieEncryptionKey;
  }
  
  public boolean csrfEnabled() {
    return true;
  }
  
  public int l10nReloadSeconds() {
    return 10;
  }
  
  public String messageFlashScopeCookieName() {
    return "fusionauth.flash-message";
  }
  
  public int savedRequestCookieMaximumSize() {
    int i = (int)(this.configuration.httpMaxHeaderSize() * 0.75F);
    return Math.min(6144, i);
  }
  
  public int templateCheckSeconds() {
    if (this.configuration.runtimeMode() == RuntimeMode.FusionAuth_Development)
      return 0; 
    return 10;
  }
  
  public List<Class<? extends Annotation>> unwrapAnnotations() {
    return new ArrayList<>(Arrays.asList((Class<? extends Annotation>[])new Class[] { JsonUnwrapped.class, FieldUnwrapped.class }));
  }
}
