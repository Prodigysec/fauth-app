package io.fusionauth.app.action.ajax.application;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.user.RegistrationFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.api.LambdaResponse;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{applicationId}", requiresAuthentication = true, constraints = {"admin", "application_manager"})
public class ViewAction extends BaseApplicationAJAXAction {
  public Key accessTokenKey;
  
  public Lambda accessTokenPopulateLambda;
  
  public Key idTokenKey;
  
  public Lambda idTokenPopulateLambda;
  
  public Key samlv2AssertionEncryptionTransportKey;
  
  public Key samlv2AuthNSigningKey;
  
  public Key samlv2AuthNVerificationKey;
  
  public Key samlv2LogoutRequestSigningKey;
  
  public Key samlv2LogoutResponseSigningKey;
  
  public Key samlv2LogoutVerificationKey;
  
  @FTLVariable
  public RegistrationFrontendService.SelfRegistrationType selfRegistrationType;
  
  public Lambda samlv2PopulateLambda;
  
  public Lambda userinfoPopulateLambda;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    retrieveApplication();
    retrieveAdditionalItemsForView();
    return "render";
  }
  
  private void retrieveAdditionalItemsForView() {
    if (this.application.registrationConfiguration.enabled) {
      this.selfRegistrationType = RegistrationFrontendService.SelfRegistrationType.Create;
    } else if (this.application.registrationConfiguration.completeRegistration) {
      this.selfRegistrationType = RegistrationFrontendService.SelfRegistrationType.Complete;
    } else {
      this.selfRegistrationType = RegistrationFrontendService.SelfRegistrationType.Disabled;
    } 
    if (this.application.jwtConfiguration != null && this.application.jwtConfiguration.accessTokenKeyId != null)
      this.accessTokenKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.application.jwtConfiguration.accessTokenKeyId))).key; 
    if (this.application.jwtConfiguration != null && this.application.jwtConfiguration.idTokenKeyId != null)
      this.idTokenKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.application.jwtConfiguration.idTokenKeyId))).key; 
    if (this.application.lambdaConfiguration != null) {
      if (this.application.lambdaConfiguration.accessTokenPopulateId != null)
        this.accessTokenPopulateLambda = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambda(this.application.lambdaConfiguration.accessTokenPopulateId))).lambda; 
      if (this.application.lambdaConfiguration.idTokenPopulateId != null)
        this.idTokenPopulateLambda = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambda(this.application.lambdaConfiguration.idTokenPopulateId))).lambda; 
      if (this.application.lambdaConfiguration.samlv2PopulateId != null)
        this.samlv2PopulateLambda = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambda(this.application.lambdaConfiguration.samlv2PopulateId))).lambda; 
      if (this.application.lambdaConfiguration.userinfoPopulateId != null)
        this.userinfoPopulateLambda = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambda(this.application.lambdaConfiguration.userinfoPopulateId))).lambda; 
    } 
    if (this.application.samlv2Configuration != null) {
      this.samlv2AuthNSigningKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.application.samlv2Configuration.keyId))).key;
      this.samlv2LogoutRequestSigningKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.application.samlv2Configuration.logout.singleLogout.keyId))).key;
      this.samlv2LogoutResponseSigningKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.application.samlv2Configuration.logout.keyId))).key;
      if (this.application.samlv2Configuration.defaultVerificationKeyId != null)
        this.samlv2AuthNVerificationKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.application.samlv2Configuration.defaultVerificationKeyId))).key; 
      if (this.application.samlv2Configuration.logout.defaultVerificationKeyId != null)
        this.samlv2LogoutVerificationKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.application.samlv2Configuration.logout.defaultVerificationKeyId))).key; 
      if (this.application.samlv2Configuration.assertionEncryptionConfiguration.keyTransportEncryptionKeyId != null)
        this.samlv2AssertionEncryptionTransportKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.application.samlv2Configuration.assertionEncryptionConfiguration.keyTransportEncryptionKeyId))).key; 
    } 
  }
}
