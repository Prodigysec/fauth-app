package io.fusionauth.api.service.jwt;

import io.fusionauth.api.service.jwt.claims.JWTAuthenticationMethods;
import io.fusionauth.api.service.jwt.claims.JWTType;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class FusionAuthJWTDecoder {
  public static ValidatedJWTResult decode(String paramString, Map<String, Verifier> paramMap, JWTConstraints paramJWTConstraints) {
    ValidatedJWTResult validatedJWTResult = decodeOnly(paramString, paramMap);
    if (validatedJWTResult.jwt == null)
      return validatedJWTResult; 
    return validateConstraints(validatedJWTResult.jwt, paramJWTConstraints);
  }
  
  public static ValidatedJWTResult decodeOnly(String paramString, Map<String, Verifier> paramMap) {
    ValidatedJWTResult validatedJWTResult = new ValidatedJWTResult();
    try {
      validatedJWTResult.jwt = JWT.getDecoder().decode(paramString, paramMap);
      validatedJWTResult.valid = true;
    } catch (Exception exception) {
      validatedJWTResult.exception = exception;
    } 
    return validatedJWTResult;
  }
  
  public static JWT unsafeDecode(String paramString) {
    JWT jWT = JWTUtils.decodePayload(paramString);
    jWT.header = JWTUtils.decodeHeader(paramString);
    return jWT;
  }
  
  public static ValidatedJWTResult validateConstraints(JWT paramJWT, JWTConstraints paramJWTConstraints) {
    Objects.requireNonNull(paramJWTConstraints);
    ValidatedJWTResult validatedJWTResult = new ValidatedJWTResult();
    validatedJWTResult.jwt = paramJWT;
    if (JWTAuthenticationMethods.contains(validatedJWTResult.jwt, "none")) {
      validatedJWTResult.reason = "The token is not suitable for the requested use.";
      validatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.noAuthentication;
      return validatedJWTResult;
    } 
    if (!paramJWTConstraints.tokenTypes.isEmpty() && !JWTType.isOneOf(validatedJWTResult.jwt, paramJWTConstraints.tokenTypes)) {
      Object object1 = validatedJWTResult.jwt.getObject("tty");
      if (object1 == null) {
        validatedJWTResult.reason = "The [tty] claim is not present.";
      } else {
        String str = paramJWTConstraints.tokenTypes.stream().map(JWTType::value).collect(Collectors.joining(", "));
        validatedJWTResult.reason = String.format("The [tty] claim [%s] is not one of the allowed values [%s].", new Object[] { object1, str });
      } 
      validatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.tokenType;
      return validatedJWTResult;
    } 
    for (String str : new TreeSet(paramJWTConstraints.restrictedClaims)) {
      if (validatedJWTResult.jwt.getObject(str) != null) {
        validatedJWTResult.reason = String.format("Found unexpected claim [%s].", new Object[] { str });
        validatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.restrictedClaim;
        validatedJWTResult.restrictedClaim = str;
        return validatedJWTResult;
      } 
    } 
    Object object = validatedJWTResult.jwt.getObject("gty");
    boolean bool = paramJWTConstraints.requiredClaims.contains("gty");
    if (bool && object == null) {
      validatedJWTResult.reason = "Missing required claim [gty].";
      validatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.requiredClaim;
      validatedJWTResult.requiredClaim = "gty";
      return validatedJWTResult;
    } 
    if (object != null) {
      List list2;
      if (object instanceof List) {
        list2 = (List)object;
        if (list2.size() == 0) {
          validatedJWTResult.reason = "The [gty] claim is invalid.";
          validatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.unexpectedClaimFormat;
          return validatedJWTResult;
        } 
      } else {
        validatedJWTResult.reason = "The [gty] claim is invalid.";
        validatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.unexpectedClaimFormat;
        return validatedJWTResult;
      } 
      List list1 = list2;
      for (Object object1 : list1) {
        if (!JWTConstraints.AllSupportedGrants.contains(GrantType.forValue(object1.toString()))) {
          validatedJWTResult.reason = String.format("The [gty] claim contains an unsupported grant type of [%s].", new Object[] { object1 });
          validatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.unsupportedGrantType;
          return validatedJWTResult;
        } 
      } 
      if (!paramJWTConstraints.allowedGrants.isEmpty()) {
        String str = list1.getFirst().toString();
        if (!paramJWTConstraints.allowedGrants.contains(GrantType.forValue(str))) {
          validatedJWTResult.reason = String.format("The [gty] claim contains an unexpected grant type of [%s].", new Object[] { str });
          validatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.unexpectedGrantType;
          return validatedJWTResult;
        } 
        if (GrantType.client_credentials.grantName().equals(str) && list1.size() != 1) {
          String str1 = list1.subList(1, list1.size()).stream().map(Object::toString).collect(Collectors.joining(", "));
          validatedJWTResult.reason = String.format("The [gty] claim contains an unexpected grant type(s) of [%s].", new Object[] { str1 });
          validatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.unexpectedGrantType;
          return validatedJWTResult;
        } 
      } 
    } 
    for (String str : new TreeSet(paramJWTConstraints.requiredClaims)) {
      Object object1 = validatedJWTResult.jwt.getObject(str);
      if (object1 == null) {
        validatedJWTResult.reason = String.format("Missing required claim [%s].", new Object[] { str });
        validatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.requiredClaim;
        validatedJWTResult.requiredClaim = str;
        return validatedJWTResult;
      } 
    } 
    validatedJWTResult.valid = true;
    return validatedJWTResult;
  }
  
  public static ValidatedJWTResult validateConstraints(String paramString, JWTConstraints paramJWTConstraints) {
    JWT jWT = unsafeDecode(paramString);
    return validateConstraints(jWT, paramJWTConstraints);
  }
  
  public static class JWTConstraints {
    public static final JWTConstraints FusionAuthOAuthUserAccessToken = newBuilder()
      .withAllowedTokenTypes(new JWTType[] { JWTType.AccessToken }).withAllowedPrimaryGrants(new GrantType[] { GrantType.authorization_code }).withRequiredClaims(new String[] { "aud", "auth_time", "exp", "gty", "iat", "iss", "sub", "tid", "tty" }).build();
    
    public static final JWTConstraints LegacyOAuthUserIdTokenForLogout = newBuilder()
      .withRequiredClaims(new String[] { "aud", "auth_time", "exp", "iat", "iss", "sub" }).withRestrictedClaims(new String[] { "tty", "gty" }).build();
    
    public static final JWTConstraints OAuthIntrospectApplicationToken = newBuilder()
      .withAllowedTokenTypes(new JWTType[] { JWTType.AccessToken, JWTType.IdToken }).withAllowedPrimaryGrants(new GrantType[] { GrantType.authorization_code, GrantType.implicit, GrantType.password, GrantType.device_code }).withRequiredClaims(new String[] { "exp", "iat", "iss", "sub", "tid", "tty" }).build();
    
    public static final JWTConstraints OAuthIntrospectEntityToken = newBuilder()
      .withAllowedTokenTypes(new JWTType[] { JWTType.AccessToken }).withAllowedPrimaryGrants(new GrantType[] { GrantType.client_credentials }).withRequiredClaims(new String[] { "exp", "gty", "iat", "iss", "sub", "tid", "tty" }).build();
    
    public static final JWTConstraints OAuthUserAccessToken = newBuilder()
      .withAllowedTokenTypes(new JWTType[] { JWTType.AccessToken }).withAllowedPrimaryGrants(new GrantType[] { GrantType.authorization_code, GrantType.implicit, GrantType.password, GrantType.device_code }).withRequiredClaims(new String[] { "aud", "auth_time", "exp", "gty", "iat", "iss", "sub", "tid", "tty" }).build();
    
    public static final JWTConstraints OAuthUserIdToken = newBuilder(OAuthUserAccessToken)
      .withAllowedTokenTypes(new JWTType[] { JWTType.IdToken }).build();
    
    public static final JWTConstraints SCIMServerAccessToken = newBuilder()
      .withAllowedTokenTypes(new JWTType[] { JWTType.AccessToken }).withAllowedPrimaryGrants(new GrantType[] { GrantType.client_credentials }).withRequiredClaims(new String[] { "aud", "exp", "gty", "iat", "iss", "sub", "tid", "tty", "use" }).build();
    
    public static final JWTConstraints UserAccessToken = newBuilder()
      .withAllowedTokenTypes(new JWTType[] { JWTType.AccessToken }).withAllowedPrimaryGrants(new GrantType[] { GrantType.authorization_code, GrantType.implicit, GrantType.password, GrantType.device_code }).withRequiredClaims(new String[] { "auth_time", "exp", "iat", "iss", "sub", "tid", "tty" }).build();
    
    public static final JWTConstraints UserAccessTokenWithAudience = newBuilder(UserAccessToken)
      .withAdditionalRequiredClaims(new String[] { "aud" }).build();
    
    private static final Set<GrantType> AllSupportedGrants = new LinkedHashSet<>(Arrays.asList(new GrantType[] { GrantType.authorization_code, GrantType.client_credentials, GrantType.device_code, GrantType.implicit, GrantType.password, GrantType.refresh_token }));
    
    private Set<GrantType> allowedGrants = Set.of();
    
    private Set<String> requiredClaims = Set.of();
    
    private Set<String> restrictedClaims = Set.of();
    
    private Set<JWTType> tokenTypes = Set.of();
    
    private JWTConstraints() {}
    
    private JWTConstraints(JWTConstraints param1JWTConstraints) {
      this.allowedGrants = Set.copyOf(param1JWTConstraints.allowedGrants);
      this.requiredClaims = Set.copyOf(param1JWTConstraints.requiredClaims);
      this.restrictedClaims = Set.copyOf(param1JWTConstraints.restrictedClaims);
      this.tokenTypes = Set.copyOf(param1JWTConstraints.tokenTypes);
    }
    
    public static Builder newBuilder() {
      return new Builder();
    }
    
    public static Builder newBuilder(JWTConstraints param1JWTConstraints) {
      return new Builder(param1JWTConstraints);
    }
    
    public static class Builder {
      private final FusionAuthJWTDecoder.JWTConstraints constraints;
      
      private Builder(FusionAuthJWTDecoder.JWTConstraints param2JWTConstraints) {
        this.constraints = new FusionAuthJWTDecoder.JWTConstraints(param2JWTConstraints);
      }
      
      private Builder() {
        this.constraints = new FusionAuthJWTDecoder.JWTConstraints();
      }
      
      public FusionAuthJWTDecoder.JWTConstraints build() {
        return this.constraints;
      }
      
      public Builder withAdditionalRequiredClaims(String... param2VarArgs) {
        HashSet hashSet = new HashSet(Set.copyOf(this.constraints.requiredClaims));
        hashSet.addAll(Set.of((Object[])param2VarArgs));
        this.constraints.requiredClaims = Set.copyOf(hashSet);
        return this;
      }
      
      public Builder withAllowedPrimaryGrants(GrantType... param2VarArgs) {
        this.constraints.allowedGrants = Set.copyOf(Set.of((Object[])param2VarArgs));
        return this;
      }
      
      public Builder withAllowedTokenTypes(JWTType... param2VarArgs) {
        this.constraints.tokenTypes = Set.copyOf(Set.of((Object[])param2VarArgs));
        return this;
      }
      
      public Builder withRequiredClaims(String... param2VarArgs) {
        this.constraints.requiredClaims = Set.copyOf(Set.of((Object[])param2VarArgs));
        return this;
      }
      
      public Builder withRestrictedClaims(String... param2VarArgs) {
        this.constraints.restrictedClaims = Set.copyOf(Set.of((Object[])param2VarArgs));
        return this;
      }
    }
  }
  
  public static class Builder {
    private final FusionAuthJWTDecoder.JWTConstraints constraints;
    
    private Builder(FusionAuthJWTDecoder.JWTConstraints param1JWTConstraints) {
      this.constraints = new FusionAuthJWTDecoder.JWTConstraints(param1JWTConstraints);
    }
    
    private Builder() {
      this.constraints = new FusionAuthJWTDecoder.JWTConstraints();
    }
    
    public FusionAuthJWTDecoder.JWTConstraints build() {
      return this.constraints;
    }
    
    public Builder withAdditionalRequiredClaims(String... param1VarArgs) {
      HashSet hashSet = new HashSet(Set.copyOf(this.constraints.requiredClaims));
      hashSet.addAll(Set.of((Object[])param1VarArgs));
      this.constraints.requiredClaims = Set.copyOf(hashSet);
      return this;
    }
    
    public Builder withAllowedPrimaryGrants(GrantType... param1VarArgs) {
      this.constraints.allowedGrants = Set.copyOf(Set.of((Object[])param1VarArgs));
      return this;
    }
    
    public Builder withAllowedTokenTypes(JWTType... param1VarArgs) {
      this.constraints.tokenTypes = Set.copyOf(Set.of((Object[])param1VarArgs));
      return this;
    }
    
    public Builder withRequiredClaims(String... param1VarArgs) {
      this.constraints.requiredClaims = Set.copyOf(Set.of((Object[])param1VarArgs));
      return this;
    }
    
    public Builder withRestrictedClaims(String... param1VarArgs) {
      this.constraints.restrictedClaims = Set.copyOf(Set.of((Object[])param1VarArgs));
      return this;
    }
  }
}
