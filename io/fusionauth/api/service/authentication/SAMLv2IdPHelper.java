package io.fusionauth.api.service.authentication;

import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.domain.SAMLv2DestinationAssertionPolicy;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import io.fusionauth.samlv2.domain.Assertion;
import io.fusionauth.samlv2.domain.AuthenticationResponse;
import io.fusionauth.samlv2.domain.NameID;
import io.fusionauth.samlv2.domain.NameIDFormat;
import io.fusionauth.samlv2.domain.Subject;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.annotation.Nullable;

public class SAMLv2IdPHelper {
  public static DateTimeFormatter SAMLv2DateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
  
  public static void assertAudience(Debugger paramDebugger, @Nullable ExternalIdentifier paramExternalIdentifier, Assertion paramAssertion, String paramString) {
    if (paramAssertion.conditions == null || paramAssertion.conditions.audiences.isEmpty())
      return; 
    commonAudienceAssertion(paramDebugger, paramExternalIdentifier, paramAssertion);
    if (paramString == null)
      return; 
    paramDebugger.log("Assert the [Audience] of the SAML response is equal to the expected [" + paramString + "].");
    for (String str : paramAssertion.conditions.audiences) {
      if (str.equals(paramString))
        return; 
    } 
    paramDebugger.log("Unable to verify the [Audience]. Expected to find [" + paramString + "] but did not find it. This SAML response is intended for one of the following audience(s) [" + String.join(",", paramAssertion.conditions.audiences) + "].")
      .done();
    if (paramExternalIdentifier != null)
      paramExternalIdentifier.data.addTraceStep("Audience verification", false, String.format("The SAML audience failed validation. Expected to find [%s], but the SAML response was intended for one of the following audiences [%s]", new Object[] { paramString, String.join(",", paramAssertion.conditions.audiences) })); 
    throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponseAudienceVerificationFailed);
  }
  
  public static void assertAudienceSuffix(Debugger paramDebugger, @Nullable ExternalIdentifier paramExternalIdentifier, Assertion paramAssertion, String paramString) {
    if (paramAssertion.conditions == null || paramAssertion.conditions.audiences.isEmpty())
      return; 
    commonAudienceAssertion(paramDebugger, paramExternalIdentifier, paramAssertion);
    paramDebugger.log("Assert the [Audience] of the SAML response is equal to the expected value ending in [" + paramString + "].");
    for (String str : paramAssertion.conditions.audiences) {
      if (str.endsWith(paramString))
        return; 
    } 
    paramDebugger.log("Unable to verify the [Audience]. Expected to find [" + paramString + "] but did not find it. This SAML response is intended for one of the following audience(s) [" + String.join(",", paramAssertion.conditions.audiences) + "].")
      .done();
    if (paramExternalIdentifier != null)
      paramExternalIdentifier.data.addTraceStep("Audience verification", false, String.format("The SAML audience failed validation. Expected to find [%s], but the SAML response was intended for one of the following audiences [%s]", new Object[] { paramString, String.join(",", paramAssertion.conditions.audiences) })); 
    throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponseAudienceVerificationFailed);
  }
  
  public static void assertDestination(Debugger paramDebugger, @Nullable ExternalIdentifier paramExternalIdentifier, AuthenticationResponse paramAuthenticationResponse, SAMLv2IdentityProvider paramSAMLv2IdentityProvider, String paramString) {
    String str = paramAuthenticationResponse.destination;
    if (str == null || paramString == null) {
      paramDebugger.log("The [Destination] attribute was not found in the SAML response. Unable to validate.");
      return;
    } 
    paramDebugger.log("Destination assertion policy [" + String.valueOf(paramSAMLv2IdentityProvider.assertionConfiguration.destination.policy) + "].")
      .log("Assert the [Destination] attribute is equal to the expected [" + paramString + "].");
    if (str.equals(paramString))
      return; 
    switch (paramSAMLv2IdentityProvider.assertionConfiguration.destination.policy) {
      case Enabled:
        paramDebugger.log("Unable to verify the [Destination]. Expected to find [" + paramString + "] but found [" + str + "].")
          .done();
        if (paramExternalIdentifier != null)
          paramExternalIdentifier.data.addTraceStep("Destination verification", false, String.format("SAML destination validation failed. Expected to find [%s] but found [%s].", new Object[] { paramString, str })); 
        throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponseDestinationVerificationFailed);
      case Disabled:
        paramDebugger.log("Unable to verify the [Destination]. Expected to find [" + paramString + "] but found [" + str + "].");
        break;
      case AllowAlternates:
        if (paramSAMLv2IdentityProvider.assertionConfiguration.destination.alternates.contains(str)) {
          paramDebugger.log("The [Destination] attribute [" + str + "] was in the configured alternates list.");
          break;
        } 
        paramDebugger.log("Unable to verify the [Destination]. [" + str + "] was not an expected or alternate destination.")
          .done();
        if (paramExternalIdentifier != null)
          paramExternalIdentifier.data.addTraceStep("Destination verification", false, String.format("SAML destination validation failed. [%s] was not an expected or alternate destination.", new Object[] { str })); 
        throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponseDestinationVerificationFailed);
    } 
  }
  
  public static void assertInResponseToNotNull(Debugger paramDebugger, AuthenticationResponse paramAuthenticationResponse) {
    if (paramAuthenticationResponse.inResponseTo == null) {
      paramDebugger.log("This SAML response was unsolicited, as it did not contain the [InResponseTo] attribute.")
        .done();
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponseUnsolicited);
    } 
  }
  
  public static void assertIssuer(Debugger paramDebugger, AuthenticationResponse paramAuthenticationResponse, String paramString) {
    String str = paramAuthenticationResponse.issuer;
    if (str == null || paramString == null)
      return; 
    paramDebugger.log("Assert the [Issuer] attribute is equal to the expected [" + paramString + "].");
    if (!str.equals(paramString)) {
      paramDebugger.log("Unable to verify the [Issuer]. Expected to find [" + paramString + "] but found [" + paramAuthenticationResponse.issuer + "].")
        .done();
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLIdPInitiatedIssuerVerificationFailed);
    } 
  }
  
  public static void assertSubjectConditions(Debugger paramDebugger, @Nullable ExternalIdentifier paramExternalIdentifier, Subject paramSubject) {
    if (paramSubject != null && paramSubject.subjectConfirmation != null) {
      paramDebugger.log("Assert the [Subject] is eligible for confirmation if [NotOnOrAfter] is defined.");
      handleNotOnOrAfter(paramDebugger, paramExternalIdentifier, paramSubject.subjectConfirmation.notOnOrAfter, "Subject", ExternalAuthenticationException.Reason.SAMLResponseSubjectNoOnOrAfterVerificationFailed);
    } 
  }
  
  public static String getAttribute(Assertion paramAssertion, String paramString) {
    if (paramString == null)
      return null; 
    List<String> list = (List)paramAssertion.attributes.get(paramString);
    if (list != null && list.size() > 0)
      return list.get(0); 
    return null;
  }
  
  public static String resolveNameId(Assertion paramAssertion, BaseIdentityProviderAuthenticationService.LoginContext paramLoginContext) {
    String str = null;
    if (paramAssertion.subject != null && paramAssertion.subject.nameIDs != null)
      for (NameID nameID : paramAssertion.subject.nameIDs) {
        if (nameID.format != null) {
          if (paramLoginContext.identityProviderUserId == null) {
            str = nameID.id;
            continue;
          } 
          if (nameID.format.equals(NameIDFormat.Persistent.toSAMLFormat()))
            str = nameID.id; 
        } 
      }  
    return str;
  }
  
  private static void commonAudienceAssertion(Debugger paramDebugger, @Nullable ExternalIdentifier paramExternalIdentifier, Assertion paramAssertion) {
    paramDebugger.log("Assert the [Audience] is eligible for confirmation if [NotBefore] is defined.");
    handleNotBefore(paramDebugger, paramExternalIdentifier, paramAssertion.conditions.notBefore, "Audience", ExternalAuthenticationException.Reason.SAMLResponseAudienceNotBeforeVerificationFailed);
    paramDebugger.log("Assert the [Audience] is eligible for confirmation if [NotOnOrAfter] is defined.");
    handleNotOnOrAfter(paramDebugger, paramExternalIdentifier, paramAssertion.conditions.notOnOrAfter, "Audience", ExternalAuthenticationException.Reason.SAMLResponseAudienceNotOnOrAfterVerificationFailed);
  }
  
  private static void handleNotBefore(Debugger paramDebugger, @Nullable ExternalIdentifier paramExternalIdentifier, ZonedDateTime paramZonedDateTime, String paramString, ExternalAuthenticationException.Reason paramReason) {
    if (paramZonedDateTime != null) {
      ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(60L);
      if (zonedDateTime.isBefore(paramZonedDateTime)) {
        paramDebugger.log("Unable to verify the [" + paramString + "] attribute. The attribute cannot be confirmed until [" + SAMLv2DateTimeFormat.format(paramZonedDateTime) + "].")
          .done();
        if (paramExternalIdentifier != null)
          paramExternalIdentifier.data.addTraceStep(String.format("%s not before", new Object[] { paramString }), false, String.format("Unable to verify the [%s] attribute. The attribute cannot be confirmed until [%s].", new Object[] { paramString, SAMLv2DateTimeFormat.format(paramZonedDateTime) })); 
        throw new ExternalAuthenticationException(paramReason);
      } 
      paramDebugger.log("The [" + paramString + "] did define a [NotBefore] constraint and it is now available for assertion.");
    } else {
      paramDebugger.log("The [" + paramString + "] attribute did not define a [NotBefore] constraint.");
    } 
  }
  
  private static void handleNotOnOrAfter(Debugger paramDebugger, @Nullable ExternalIdentifier paramExternalIdentifier, ZonedDateTime paramZonedDateTime, String paramString, ExternalAuthenticationException.Reason paramReason) {
    if (paramZonedDateTime != null) {
      ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(60L);
      if (zonedDateTime.isAfter(paramZonedDateTime)) {
        paramDebugger.log("Unable to verify the [" + paramString + "] attribute. The attribute cannot be confirmed after [" + SAMLv2DateTimeFormat.format(paramZonedDateTime) + "].")
          .done();
        if (paramExternalIdentifier != null)
          paramExternalIdentifier.data.addTraceStep(String.format("%s not on or after", new Object[] { paramString }), false, String.format("Unable to verify the [%s] attribute. The attribute cannot be confirmed after [%s].", new Object[] { paramString, SAMLv2DateTimeFormat.format(paramZonedDateTime) })); 
        throw new ExternalAuthenticationException(paramReason);
      } 
      paramDebugger.log("The [" + paramString + "] did define a [NotOnOrAfter] constraint and it is still available for assertion.");
    } else {
      paramDebugger.log("The [" + paramString + "] attribute did not define a [NoOnOrAfter] constraint.");
    } 
  }
}
