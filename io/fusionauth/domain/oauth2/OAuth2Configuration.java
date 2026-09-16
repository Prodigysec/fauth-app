package io.fusionauth.domain.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.util.Normalizer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class OAuth2Configuration implements Buildable<OAuth2Configuration> {
  @JsonMerge(OptBoolean.FALSE)
  public List<URI> authorizedOriginURLs = new ArrayList<>();
  
  @JsonMerge(OptBoolean.FALSE)
  public List<URI> authorizedRedirectURLs = new ArrayList<>();
  
  @JsonMerge(OptBoolean.FALSE)
  public List<URI> authorizedResourceUris = new ArrayList<>();
  
  public Oauth2AuthorizedURLValidationPolicy authorizedURLValidationPolicy = Oauth2AuthorizedURLValidationPolicy.ExactMatch;
  
  public ClientAuthenticationPolicy clientAuthenticationPolicy;
  
  public String clientId;
  
  @MaskString
  public String clientSecret;
  
  public OAuthScopeConsentMode consentMode = OAuthScopeConsentMode.AlwaysPrompt;
  
  public boolean debug;
  
  public URI deviceVerificationURL;
  
  @JsonMerge(OptBoolean.FALSE)
  public Set<GrantType> enabledGrants = new TreeSet<>(
      Comparator.comparing(GrantType::grantName));
  
  public boolean generateRefreshTokens;
  
  public LogoutBehavior logoutBehavior = LogoutBehavior.AllApplications;
  
  public URI logoutURL;
  
  public ProofKeyForCodeExchangePolicy proofKeyForCodeExchangePolicy = ProofKeyForCodeExchangePolicy.NotRequired;
  
  public ProvidedScopePolicy providedScopePolicy = new ProvidedScopePolicy();
  
  public OAuthApplicationRelationship relationship = OAuthApplicationRelationship.FirstParty;
  
  @Deprecated
  public boolean requireClientAuthentication = true;
  
  public boolean requireRegistration;
  
  public OAuthScopeHandlingPolicy scopeHandlingPolicy = OAuthScopeHandlingPolicy.Strict;
  
  public UnknownScopePolicy unknownScopePolicy = UnknownScopePolicy.Reject;
  
  public OAuth2Configuration(OAuth2Configuration paramOAuth2Configuration) {
    this.authorizedOriginURLs.addAll(paramOAuth2Configuration.authorizedOriginURLs);
    this.authorizedRedirectURLs.addAll(paramOAuth2Configuration.authorizedRedirectURLs);
    this.authorizedResourceUris.addAll(paramOAuth2Configuration.authorizedResourceUris);
    this.authorizedURLValidationPolicy = paramOAuth2Configuration.authorizedURLValidationPolicy;
    this.clientAuthenticationPolicy = paramOAuth2Configuration.clientAuthenticationPolicy;
    this.clientId = paramOAuth2Configuration.clientId;
    this.clientSecret = paramOAuth2Configuration.clientSecret;
    this.consentMode = paramOAuth2Configuration.consentMode;
    this.debug = paramOAuth2Configuration.debug;
    this.deviceVerificationURL = paramOAuth2Configuration.deviceVerificationURL;
    this.enabledGrants.addAll(paramOAuth2Configuration.enabledGrants);
    this.generateRefreshTokens = paramOAuth2Configuration.generateRefreshTokens;
    this.logoutBehavior = paramOAuth2Configuration.logoutBehavior;
    this.logoutURL = paramOAuth2Configuration.logoutURL;
    this.providedScopePolicy = new ProvidedScopePolicy(paramOAuth2Configuration.providedScopePolicy);
    this.proofKeyForCodeExchangePolicy = paramOAuth2Configuration.proofKeyForCodeExchangePolicy;
    this.relationship = paramOAuth2Configuration.relationship;
    this.requireClientAuthentication = paramOAuth2Configuration.requireClientAuthentication;
    this.requireRegistration = paramOAuth2Configuration.requireRegistration;
    this.scopeHandlingPolicy = paramOAuth2Configuration.scopeHandlingPolicy;
    this.unknownScopePolicy = paramOAuth2Configuration.unknownScopePolicy;
  }
  
  public OAuth2Configuration(String paramString1, String paramString2) {
    this.clientId = paramString1;
    this.clientSecret = paramString2;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof OAuth2Configuration))
      return false; 
    OAuth2Configuration oAuth2Configuration = (OAuth2Configuration)paramObject;
    return (this.debug == oAuth2Configuration.debug && this.generateRefreshTokens == oAuth2Configuration.generateRefreshTokens && this.requireClientAuthentication == oAuth2Configuration.requireClientAuthentication && this.requireRegistration == oAuth2Configuration.requireRegistration && 


      
      Objects.equals(this.authorizedOriginURLs, oAuth2Configuration.authorizedOriginURLs) && 
      Objects.equals(this.authorizedRedirectURLs, oAuth2Configuration.authorizedRedirectURLs) && 
      Objects.equals(this.authorizedResourceUris, oAuth2Configuration.authorizedResourceUris) && 
      Objects.equals(this.authorizedURLValidationPolicy, oAuth2Configuration.authorizedURLValidationPolicy) && 
      Objects.equals(this.clientAuthenticationPolicy, oAuth2Configuration.clientAuthenticationPolicy) && 
      Objects.equals(this.clientId, oAuth2Configuration.clientId) && 
      Objects.equals(this.clientSecret, oAuth2Configuration.clientSecret) && 
      Objects.equals(this.consentMode, oAuth2Configuration.consentMode) && 
      Objects.equals(this.deviceVerificationURL, oAuth2Configuration.deviceVerificationURL) && 
      Objects.equals(this.enabledGrants, oAuth2Configuration.enabledGrants) && 
      Objects.equals(this.logoutBehavior, oAuth2Configuration.logoutBehavior) && 
      Objects.equals(this.logoutURL, oAuth2Configuration.logoutURL) && 
      Objects.equals(this.providedScopePolicy, oAuth2Configuration.providedScopePolicy) && 
      Objects.equals(this.proofKeyForCodeExchangePolicy, oAuth2Configuration.proofKeyForCodeExchangePolicy) && 
      Objects.equals(this.relationship, oAuth2Configuration.relationship) && 
      Objects.equals(this.scopeHandlingPolicy, oAuth2Configuration.scopeHandlingPolicy) && 
      Objects.equals(this.unknownScopePolicy, oAuth2Configuration.unknownScopePolicy));
  }
  
  @JsonIgnore
  public URI getFirstAuthorizedRedirectURLIgnoringPatterns() {
    return this.authorizedRedirectURLs.stream()
      .filter(paramURI -> !paramURI.toString().contains("*"))
      .findFirst()
      .orElse(null);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.authorizedOriginURLs, this.authorizedRedirectURLs, this.authorizedResourceUris, this.authorizedURLValidationPolicy, this.clientAuthenticationPolicy, this.clientId, this.clientSecret, this.consentMode, Boolean.valueOf(this.debug), this.deviceVerificationURL, 
          this.enabledGrants, Boolean.valueOf(this.generateRefreshTokens), this.logoutBehavior, this.logoutURL, this.providedScopePolicy, this.proofKeyForCodeExchangePolicy, this.relationship, Boolean.valueOf(this.requireClientAuthentication), Boolean.valueOf(this.requireRegistration), this.scopeHandlingPolicy, 
          this.unknownScopePolicy });
  }
  
  public void normalize() {
    Normalizer.removeEmpty(this.authorizedOriginURLs);
    Normalizer.removeEmpty(this.authorizedRedirectURLs);
    Normalizer.removeEmpty(this.authorizedResourceUris);
    this.clientId = Normalizer.trim(this.clientId);
    this.clientSecret = Normalizer.trim(this.clientSecret);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  @JacksonConstructor
  public OAuth2Configuration() {}
}
