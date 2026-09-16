package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.webauthn.AttestationType;
import io.fusionauth.domain.webauthn.CoseAlgorithmIdentifier;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class WebAuthnCredential implements Tenantable, Buildable<WebAuthnCredential>, JSONColumnable {
  @JSONColumn
  public CoseAlgorithmIdentifier algorithm;
  
  @JSONColumn
  public AttestationType attestationType;
  
  @JSONColumn
  public boolean authenticatorSupportsUserVerification;
  
  public String credentialId;
  
  public Map<String, Object> data = new LinkedHashMap<>();
  
  @JSONColumn
  public boolean discoverable;
  
  @JSONColumn
  public String displayName;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUseInstant;
  
  @JSONColumn
  public String name;
  
  @JSONColumn
  public String publicKey;
  
  @JSONColumn
  public String relyingPartyId;
  
  @JSONColumn
  public int signCount;
  
  public UUID tenantId;
  
  @JSONColumn
  public List<String> transports = new ArrayList<>();
  
  @JSONColumn
  public String userAgent;
  
  public UUID userId;
  
  @JacksonConstructor
  public WebAuthnCredential() {}
  
  public WebAuthnCredential(WebAuthnCredential paramWebAuthnCredential) {
    this.algorithm = paramWebAuthnCredential.algorithm;
    this.attestationType = paramWebAuthnCredential.attestationType;
    this.authenticatorSupportsUserVerification = paramWebAuthnCredential.authenticatorSupportsUserVerification;
    this.credentialId = paramWebAuthnCredential.credentialId;
    if (paramWebAuthnCredential.data != null)
      this.data.putAll(paramWebAuthnCredential.data); 
    this.discoverable = paramWebAuthnCredential.discoverable;
    this.displayName = paramWebAuthnCredential.displayName;
    this.id = paramWebAuthnCredential.id;
    this.insertInstant = paramWebAuthnCredential.insertInstant;
    this.name = paramWebAuthnCredential.name;
    this.lastUseInstant = paramWebAuthnCredential.lastUseInstant;
    this.publicKey = paramWebAuthnCredential.publicKey;
    this.relyingPartyId = paramWebAuthnCredential.relyingPartyId;
    this.signCount = paramWebAuthnCredential.signCount;
    this.tenantId = paramWebAuthnCredential.tenantId;
    if (paramWebAuthnCredential.transports != null)
      this.transports.addAll(paramWebAuthnCredential.transports); 
    this.userAgent = paramWebAuthnCredential.userAgent;
    this.userId = paramWebAuthnCredential.userId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    WebAuthnCredential webAuthnCredential = (WebAuthnCredential)paramObject;
    return (this.authenticatorSupportsUserVerification == webAuthnCredential.authenticatorSupportsUserVerification && this.discoverable == webAuthnCredential.discoverable && this.signCount == webAuthnCredential.signCount && this.algorithm == webAuthnCredential.algorithm && this.attestationType == webAuthnCredential.attestationType && 



      
      Objects.equals(this.credentialId, webAuthnCredential.credentialId) && 
      Objects.equals(this.data, webAuthnCredential.data) && 
      Objects.equals(this.displayName, webAuthnCredential.displayName) && 
      Objects.equals(this.id, webAuthnCredential.id) && 
      Objects.equals(this.insertInstant, webAuthnCredential.insertInstant) && 
      Objects.equals(this.lastUseInstant, webAuthnCredential.lastUseInstant) && 
      Objects.equals(this.name, webAuthnCredential.name) && 
      Objects.equals(this.publicKey, webAuthnCredential.publicKey) && 
      Objects.equals(this.relyingPartyId, webAuthnCredential.relyingPartyId) && 
      Objects.equals(this.tenantId, webAuthnCredential.tenantId) && 
      Objects.equals(this.transports, webAuthnCredential.transports) && 
      Objects.equals(this.userAgent, webAuthnCredential.userAgent) && 
      Objects.equals(this.userId, webAuthnCredential.userId));
  }
  
  public UUID getTenantId() {
    return this.tenantId;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.algorithm, this.attestationType, 
          
          Boolean.valueOf(this.authenticatorSupportsUserVerification), this.credentialId, this.data, 

          
          Boolean.valueOf(this.discoverable), this.displayName, this.id, this.insertInstant, this.lastUseInstant, 
          this.name, this.publicKey, this.relyingPartyId, 






          
          Integer.valueOf(this.signCount), this.tenantId, this.transports, this.userAgent, this.userId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
