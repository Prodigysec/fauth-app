package io.fusionauth.app.action.admin.identityProvider;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.CanonicalizationMethod;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.SAMLv2DestinationAssertionPolicy;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.api.LambdaResponse;
import io.fusionauth.domain.provider.AppleIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.BaseSAMLv2IdentityProvider;
import io.fusionauth.domain.provider.DomainBasedIdentityProvider;
import io.fusionauth.domain.provider.EpicGamesIdentityProvider;
import io.fusionauth.domain.provider.ExternalJWTIdentityProvider;
import io.fusionauth.domain.provider.FacebookIdentityProvider;
import io.fusionauth.domain.provider.GoogleIdentityProvider;
import io.fusionauth.domain.provider.HYPRIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderLinkingStrategy;
import io.fusionauth.domain.provider.IdentityProviderLoginMethod;
import io.fusionauth.domain.provider.IdentityProviderOauth2Configuration;
import io.fusionauth.domain.provider.IdentityProviderTenantConfiguration;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.LinkedInIdentityProvider;
import io.fusionauth.domain.provider.NintendoIdentityProvider;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdPInitiatedIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import io.fusionauth.domain.provider.SonyPSNIdentityProvider;
import io.fusionauth.domain.provider.SteamAPIMode;
import io.fusionauth.domain.provider.SteamIdentityProvider;
import io.fusionauth.domain.provider.TwitchIdentityProvider;
import io.fusionauth.domain.provider.TwitterIdentityProvider;
import io.fusionauth.domain.provider.XboxIdentityProvider;
import io.fusionauth.samlv2.domain.NameIDFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

public abstract class BaseFormAction extends BaseAction {
  @FTLVariable
  public static final CanonicalizationMethod[] c14nMethods = CanonicalizationMethod.values();
  
  @FTLVariable
  public static final IdentityProviderOauth2Configuration.ClientAuthenticationMethod[] clientAuthenticationMethods = new IdentityProviderOauth2Configuration.ClientAuthenticationMethod[] { IdentityProviderOauth2Configuration.ClientAuthenticationMethod.client_secret_basic, IdentityProviderOauth2Configuration.ClientAuthenticationMethod.client_secret_post, IdentityProviderOauth2Configuration.ClientAuthenticationMethod.none };
  
  @FTLVariable
  public static final SAMLv2DestinationAssertionPolicy[] destinationAssertionPolicies = SAMLv2DestinationAssertionPolicy.values();
  
  @FTLVariable
  public static final IdentityProviderLinkingStrategy[] linkingStrategies;
  
  static {
    linkingStrategies = (IdentityProviderLinkingStrategy[])Arrays.<IdentityProviderLinkingStrategy>stream(IdentityProviderLinkingStrategy.values()).filter(paramIdentityProviderLinkingStrategy -> (paramIdentityProviderLinkingStrategy != IdentityProviderLinkingStrategy.Unsupported)).toList().toArray((Object[])new IdentityProviderLinkingStrategy[0]);
  }
  
  @FTLVariable
  public static final SteamAPIMode[] steamAPIModes = SteamAPIMode.values();
  
  public final List<Key> appleSigningKeys = new ArrayList<>();
  
  public final List<Application> applications = new ArrayList<>();
  
  public final List<Key> decryptionKeys = new ArrayList<>();
  
  public final List<Key> externalJwtVerificationKeys = new ArrayList<>();
  
  public final List<Lambda> lambdas = new ArrayList<>();
  
  @FTLVariable
  public final List<IdentityProviderLoginMethod> loginMethods = new ArrayList<>();
  
  public final List<Key> requestSigningKeys = new ArrayList<>();
  
  public final List<Key> samlv2VerificationKeys = new ArrayList<>();
  
  @FTLVariable
  public IdentityProviderTenantConfiguration defaultIdentityProviderTenantConfiguration = new IdentityProviderTenantConfiguration();
  
  public String domains;
  
  public BaseIdentityProvider<?> identityProvider;
  
  public UUID identityProviderId;
  
  public String otherNameIdFormat;
  
  public String redirectURL;
  
  @PreParameter
  public IdentityProviderType type;
  
  public boolean useOpenIdDiscovery;
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.errorMapping.put("identityProvider.domains", "domains");
    this.redirectURL = paramFrontEndSupport.getFusionAuthBaseURL() + "/oauth2/callback";
    paramFrontEndSupport.customPreTransferErrorConsumer = (paramErrors -> paramFrontEndSupport.moveFieldErrorToGeneral(paramErrors, "identityProvider.type", "[notLicensed]identityProvider.type"));
  }
  
  @FormPrepareMethod
  public void prepareApplications() {
    LambdaType lambdaType;
    ApplicationResponse applicationResponse1 = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.setTenantId(this.identityProvider.tenantId).retrieveApplications());
    if (applicationResponse1 != null && applicationResponse1.applications != null)
      this.applications.addAll(applicationResponse1.applications); 
    ApplicationResponse applicationResponse2 = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.setTenantId(this.identityProvider.tenantId).retrieveInactiveApplications());
    if (applicationResponse2 != null && applicationResponse2.applications != null)
      this.applications.addAll(applicationResponse2.applications); 
    this.applications.sort(Comparator.comparing(paramApplication -> paramApplication.name));
    if (this.identityProvider.tenantId != null && !this.identityProvider.tenantConfiguration.containsKey(this.identityProvider.tenantId))
      this.identityProvider.tenantConfiguration.put(this.identityProvider.tenantId, new IdentityProviderTenantConfiguration()); 
    switch (this.type) {
      case Apple:
        lambdaType = LambdaType.AppleReconcile;
        break;
      case EpicGames:
        lambdaType = LambdaType.EpicGamesReconcile;
        break;
      case ExternalJWT:
        lambdaType = LambdaType.ExternalJWTReconcile;
        break;
      case Facebook:
        lambdaType = LambdaType.FacebookReconcile;
        break;
      case Google:
        lambdaType = LambdaType.GoogleReconcile;
        break;
      case HYPR:
        lambdaType = LambdaType.HYPRReconcile;
        break;
      case LinkedIn:
        lambdaType = LambdaType.LinkedInReconcile;
        break;
      case Nintendo:
        lambdaType = LambdaType.NintendoReconcile;
        break;
      case OpenIDConnect:
        lambdaType = LambdaType.OpenIDReconcile;
        break;
      case SAMLv2:
      case SAMLv2IdPInitiated:
        lambdaType = LambdaType.SAMLv2Reconcile;
        break;
      case SonyPSN:
        lambdaType = LambdaType.SonyPSNReconcile;
        break;
      case Steam:
        lambdaType = LambdaType.SteamReconcile;
        break;
      case Twitch:
        lambdaType = LambdaType.TwitchReconcile;
        break;
      case Twitter:
        lambdaType = LambdaType.TwitterReconcile;
        break;
      case Xbox:
        lambdaType = LambdaType.XboxReconcile;
        break;
      default:
        throw new IllegalStateException("Illegal type of Lambda [" + String.valueOf(this.type) + "]");
    } 
    this.lambdas.addAll((Collection<? extends Lambda>)Objects.requireNonNullElseGet(((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambdasByType(paramLambdaType))).lambdas, Collections::emptyList));
    List list = (List)Objects.requireNonNullElseGet(((KeyResponse)superDelegate().execute(FusionAuthClient::retrieveKeys)).keys, Collections::emptyList);
    if (this.type == IdentityProviderType.Apple) {
      this.appleSigningKeys.addAll(list.stream().filter(KeyValidator::validForAppleSigning).toList());
    } else if (this.type == IdentityProviderType.ExternalJWT) {
      ExternalJWTIdentityProvider externalJWTIdentityProvider = (ExternalJWTIdentityProvider)this.identityProvider;
      Objects.requireNonNull(this.externalJwtVerificationKeys);
      externalJWTIdentityProvider.verificationKeyIds.stream().map(this::fetchKey).flatMap(Optional::stream).forEach(this.externalJwtVerificationKeys::add);
    } else if (this.type == IdentityProviderType.SAMLv2 || this.type == IdentityProviderType.SAMLv2IdPInitiated) {
      BaseSAMLv2IdentityProvider baseSAMLv2IdentityProvider = (BaseSAMLv2IdentityProvider)this.identityProvider;
      Objects.requireNonNull(this.samlv2VerificationKeys);
      baseSAMLv2IdentityProvider.verificationKeyIds.stream().map(this::fetchKey).flatMap(Optional::stream).forEach(this.samlv2VerificationKeys::add);
      this.requestSigningKeys.addAll(list.stream().filter(KeyValidator::validForSAMLSigning).toList());
      this.decryptionKeys.addAll(list.stream().filter(KeyValidator::validForSAMLDecryption).toList());
      if (this.type == IdentityProviderType.SAMLv2) {
        SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)this.identityProvider;
        if (!NameIDFormat.EmailAddress.toSAMLFormat().equals(sAMLv2IdentityProvider.nameIdFormat) && !NameIDFormat.Persistent.toSAMLFormat().equals(sAMLv2IdentityProvider.nameIdFormat)) {
          this.otherNameIdFormat = sAMLv2IdentityProvider.nameIdFormat;
          sAMLv2IdentityProvider.nameIdFormat = "";
        } 
      } 
    } 
    if (this.type == IdentityProviderType.Google) {
      this.loginMethods.addAll(List.of(IdentityProviderLoginMethod.UsePopup, IdentityProviderLoginMethod.UseRedirect, IdentityProviderLoginMethod.UseVendorJavaScript));
    } else if (this.type == IdentityProviderType.Facebook) {
      this.loginMethods.addAll(List.of(IdentityProviderLoginMethod.UsePopup, IdentityProviderLoginMethod.UseRedirect));
    } 
  }
  
  @PreParameterMethod
  public void setupIdentityProvider() {
    if (this.type == null)
      return; 
    switch (this.type) {
      case Apple:
        this.identityProvider = new AppleIdentityProvider();
        break;
      case ExternalJWT:
        this.identityProvider = new ExternalJWTIdentityProvider();
        break;
      case EpicGames:
        this.identityProvider = new EpicGamesIdentityProvider();
        break;
      case Facebook:
        this.identityProvider = new FacebookIdentityProvider();
        if (this.frontEndSupport.isGET() && this instanceof AddAction)
          ((FacebookIdentityProvider)this.identityProvider).loginMethod = IdentityProviderLoginMethod.UseRedirect; 
        break;
      case Google:
        this.identityProvider = new GoogleIdentityProvider();
        if (this.frontEndSupport.isGET() && this instanceof AddAction)
          ((GoogleIdentityProvider)this.identityProvider).loginMethod = IdentityProviderLoginMethod.UseRedirect; 
        break;
      case HYPR:
        this.identityProvider = new HYPRIdentityProvider();
        break;
      case LinkedIn:
        this.identityProvider = new LinkedInIdentityProvider();
        if (this.frontEndSupport.isGET() && this instanceof AddAction)
          ((LinkedInIdentityProvider)this.identityProvider).scope = "openid profile email"; 
        break;
      case Nintendo:
        this.identityProvider = new NintendoIdentityProvider();
        break;
      case OpenIDConnect:
        this.identityProvider = new OpenIdConnectIdentityProvider();
        break;
      case SAMLv2:
        this.identityProvider = new SAMLv2IdentityProvider();
        break;
      case SAMLv2IdPInitiated:
        this.identityProvider = new SAMLv2IdPInitiatedIdentityProvider();
        break;
      case SonyPSN:
        this.identityProvider = new SonyPSNIdentityProvider();
        break;
      case Steam:
        this.identityProvider = new SteamIdentityProvider();
        break;
      case Twitch:
        this.identityProvider = new TwitchIdentityProvider();
        break;
      case Twitter:
        this.identityProvider = new TwitterIdentityProvider();
        break;
      case Xbox:
        this.identityProvider = new XboxIdentityProvider();
        break;
    } 
    if (this.frontEndSupport.isGET() && this instanceof AddAction) {
      this.identityProvider.enabled = true;
      this.identityProvider.source = null;
    } 
  }
  
  @ValidationMethod
  public void validate() {
    if (this.identityProvider.getType() == IdentityProviderType.OpenIDConnect) {
      OpenIdConnectIdentityProvider openIdConnectIdentityProvider = (OpenIdConnectIdentityProvider)this.identityProvider;
      if (!this.useOpenIdDiscovery)
        openIdConnectIdentityProvider.oauth2.issuer = null; 
    } 
    Collection<? extends String> collection = CollectionTools.stringToCollection(this.domains);
    if (collection == null) {
      this.frontEndSupport.addFieldError("domains", "[invalid]domains", new Object[0]);
    } else if (this.identityProvider instanceof DomainBasedIdentityProvider) {
      ((DomainBasedIdentityProvider)this.identityProvider).getDomains().addAll(collection);
    } 
    BaseIdentityProvider<?> baseIdentityProvider = this.identityProvider;
    if (baseIdentityProvider instanceof SAMLv2IdentityProvider) {
      SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)baseIdentityProvider;
      sAMLv2IdentityProvider.nameIdFormat = (sAMLv2IdentityProvider.nameIdFormat != null && sAMLv2IdentityProvider.nameIdFormat.length() > 0) ? sAMLv2IdentityProvider.nameIdFormat : this.otherNameIdFormat;
    } 
  }
  
  private Optional<Key> fetchKey(UUID paramUUID) {
    ClientResponse<KeyResponse, Errors> clientResponse = this.superClient.retrieveKey(paramUUID);
    if (clientResponse.wasSuccessful())
      return Optional.of(((KeyResponse)clientResponse.successResponse).key); 
    return Optional.empty();
  }
}
