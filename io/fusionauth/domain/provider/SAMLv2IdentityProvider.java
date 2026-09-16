package io.fusionauth.domain.provider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.CORSConfiguration;
import io.fusionauth.domain.CanonicalizationMethod;
import io.fusionauth.domain.RequiresCORSConfiguration;
import io.fusionauth.domain.util.HTTPMethod;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class SAMLv2IdentityProvider extends BaseSAMLv2IdentityProvider<SAMLv2ApplicationConfiguration> implements Buildable<SAMLv2IdentityProvider>, DomainBasedIdentityProvider, RequiresCORSConfiguration, SupportsPostBindings {
  public final Set<String> domains = new LinkedHashSet<>();
  
  @JSONColumn
  public SAMLv2AssertionConfiguration assertionConfiguration = new SAMLv2AssertionConfiguration();
  
  @JSONColumn
  public URI buttonImageURL;
  
  @JSONColumn
  public String buttonText = "Login with SAML";
  
  @JSONColumn
  public URI idpEndpoint;
  
  @JSONColumn
  public SAMLv2IdpInitiatedConfiguration idpInitiatedConfiguration = new SAMLv2IdpInitiatedConfiguration(false);
  
  @JSONColumn
  public String issuer;
  
  @JSONColumn
  public LoginHintConfiguration loginHintConfiguration = new LoginHintConfiguration(true);
  
  @JSONColumn
  public String nameIdFormat = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
  
  @JSONColumn
  public boolean postRequest;
  
  public UUID requestSigningKeyId;
  
  @JSONColumn
  public boolean signRequest;
  
  @JSONColumn
  public CanonicalizationMethod xmlSignatureC14nMethod;
  
  @JsonIgnore
  public CORSConfiguration corsConfiguration() {
    return (new CORSConfiguration()).with(paramCORSConfiguration -> paramCORSConfiguration.allowedMethods.add(HTTPMethod.POST))
      .with(paramCORSConfiguration -> paramCORSConfiguration.allowedOrigins.add(URI.create(this.idpEndpoint.getScheme() + "://" + this.idpEndpoint.getScheme() + this.idpEndpoint.getHost())));
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof SAMLv2IdentityProvider))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)paramObject;
    return (this.postRequest == sAMLv2IdentityProvider.postRequest && this.signRequest == sAMLv2IdentityProvider.signRequest && 
      
      Objects.equals(this.domains, sAMLv2IdentityProvider.domains) && 
      Objects.equals(this.assertionConfiguration, sAMLv2IdentityProvider.assertionConfiguration) && 
      Objects.equals(this.buttonImageURL, sAMLv2IdentityProvider.buttonImageURL) && 
      Objects.equals(this.buttonText, sAMLv2IdentityProvider.buttonText) && 
      Objects.equals(this.idpEndpoint, sAMLv2IdentityProvider.idpEndpoint) && 
      Objects.equals(this.issuer, sAMLv2IdentityProvider.issuer) && 
      Objects.equals(this.loginHintConfiguration, sAMLv2IdentityProvider.loginHintConfiguration) && 
      Objects.equals(this.nameIdFormat, sAMLv2IdentityProvider.nameIdFormat) && 
      Objects.equals(this.requestSigningKeyId, sAMLv2IdentityProvider.requestSigningKeyId) && 
      Objects.equals(this.idpInitiatedConfiguration, sAMLv2IdentityProvider.idpInitiatedConfiguration) && this.xmlSignatureC14nMethod == sAMLv2IdentityProvider.xmlSignatureC14nMethod);
  }
  
  public Set<String> getDomains() {
    return this.domains;
  }
  
  public IdentityProviderType getType() {
    return IdentityProviderType.SAMLv2;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          Integer.valueOf(super.hashCode()), this.domains, this.assertionConfiguration, this.buttonImageURL, this.buttonText, this.idpEndpoint, this.issuer, this.loginHintConfiguration, this.nameIdFormat, 







          
          Boolean.valueOf(this.postRequest), 
          this.requestSigningKeyId, 
          
          Boolean.valueOf(this.signRequest), this.idpInitiatedConfiguration, this.xmlSignatureC14nMethod });
  }
  
  public URI lookupButtonImageURL(String paramString) {
    return (URI)lookup(() -> this.buttonImageURL, () -> (URI)app(paramString, ()));
  }
  
  public URI lookupButtonImageURL(UUID paramUUID) {
    return (URI)lookup(() -> this.buttonImageURL, () -> (URI)app(paramUUID, ()));
  }
  
  public String lookupButtonText(String paramString) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramString, ()));
  }
  
  public String lookupButtonText(UUID paramUUID) {
    return (String)lookup(() -> this.buttonText, () -> (String)app(paramUUID, ()));
  }
  
  public void normalize() {
    super.normalize();
    normalizeDomains();
  }
  
  public boolean postRequestEnabled() {
    return this.postRequest;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
