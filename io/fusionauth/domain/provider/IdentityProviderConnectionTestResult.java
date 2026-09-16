package io.fusionauth.domain.provider;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class IdentityProviderConnectionTestResult implements Buildable<IdentityProviderConnectionTestResult> {
  public String email;
  
  public UUID identityProviderId;
  
  public String identityProviderUserId;
  
  public ZonedDateTime startInstant;
  
  public List<IdentityProviderLoginStep> steps = new ArrayList<>();
  
  public boolean success;
  
  public String username;
  
  public IdentityProviderConnectionTestResult() {}
  
  public IdentityProviderConnectionTestResult(IdentityProviderConnectionTestResult paramIdentityProviderConnectionTestResult) {
    this.email = paramIdentityProviderConnectionTestResult.email;
    this.identityProviderId = paramIdentityProviderConnectionTestResult.identityProviderId;
    this.identityProviderUserId = paramIdentityProviderConnectionTestResult.identityProviderUserId;
    this.startInstant = paramIdentityProviderConnectionTestResult.startInstant;
    this.steps.addAll(paramIdentityProviderConnectionTestResult.steps);
    this.success = paramIdentityProviderConnectionTestResult.success;
    this.username = paramIdentityProviderConnectionTestResult.username;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    IdentityProviderConnectionTestResult identityProviderConnectionTestResult = (IdentityProviderConnectionTestResult)paramObject;
    return (Objects.equals(this.email, identityProviderConnectionTestResult.email) && 
      Objects.equals(this.identityProviderId, identityProviderConnectionTestResult.identityProviderId) && 
      Objects.equals(this.identityProviderUserId, identityProviderConnectionTestResult.identityProviderUserId) && this.success == identityProviderConnectionTestResult.success && 
      
      Objects.equals(this.startInstant, identityProviderConnectionTestResult.startInstant) && 
      Objects.equals(this.steps, identityProviderConnectionTestResult.steps) && 
      Objects.equals(this.username, identityProviderConnectionTestResult.username));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.email, this.identityProviderId, this.identityProviderUserId, this.startInstant, this.steps, Boolean.valueOf(this.success), this.username });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public static class IdentityProviderLoginStep {
    public String detail;
    
    public boolean success;
    
    public String title;
    
    @JacksonConstructor
    public IdentityProviderLoginStep() {}
    
    public IdentityProviderLoginStep(String param1String1, boolean param1Boolean, String param1String2) {
      this.title = param1String1;
      this.success = param1Boolean;
      this.detail = param1String2;
    }
    
    public boolean equals(Object param1Object) {
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      IdentityProviderLoginStep identityProviderLoginStep = (IdentityProviderLoginStep)param1Object;
      return (this.success == identityProviderLoginStep.success && Objects.equals(this.detail, identityProviderLoginStep.detail) && Objects.equals(this.title, identityProviderLoginStep.title));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.detail, Boolean.valueOf(this.success), this.title });
    }
  }
}
