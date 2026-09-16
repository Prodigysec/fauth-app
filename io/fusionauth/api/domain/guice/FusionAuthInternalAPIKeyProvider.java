package io.fusionauth.api.domain.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.fusionauth.api.domain.InstanceMapper;
import io.fusionauth.api.service.system.APIKeyReaderService;
import io.fusionauth.domain.APIKey;
import java.util.UUID;

public class FusionAuthInternalAPIKeyProvider implements Provider<APIKey> {
  private final APIKeyReaderService apiKeyReader;
  
  private final InstanceMapper instanceMapper;
  
  @Inject
  public FusionAuthInternalAPIKeyProvider(APIKeyReaderService paramAPIKeyReaderService, InstanceMapper paramInstanceMapper) {
    this.apiKeyReader = paramAPIKeyReaderService;
    this.instanceMapper = paramInstanceMapper;
  }
  
  public APIKey get() {
    UUID uUID = this.instanceMapper.retrieveInternalAuthenticationKeyId();
    if (uUID == null)
      throw new IllegalStateException("FusionAuth instance is misconfigured. No internal authentication API key Id was found for this instance."); 
    APIKey aPIKey = this.apiKeyReader.retrieveById(null, uUID);
    if (aPIKey == null)
      throw new IllegalStateException("FusionAuth instance is misconfigured. No internal authentication API key was found for Id [" + String.valueOf(uUID) + "]."); 
    return aPIKey;
  }
}
