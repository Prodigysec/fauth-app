package io.fusionauth.domain.api.identity.verify;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.VerificationStrategy;
import java.util.Map;
import java.util.UUID;

public class VerifyStartRequest implements Buildable<VerifyStartRequest> {
  public UUID applicationId;
  
  public ExistingUserStrategy existingUserStrategy = ExistingUserStrategy.mustExist;
  
  public String loginId;
  
  public String loginIdType;
  
  public Map<String, Object> state;
  
  public VerificationStrategy verificationStrategy;
}
