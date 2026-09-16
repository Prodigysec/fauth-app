package io.fusionauth.app.action.admin.application;

import com.inversoft.cache.Cache;
import com.inversoft.error.Error;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.api.service.moderation.cleanspeak.Application;
import io.fusionauth.api.service.moderation.cleanspeak.ApplicationResponse;
import io.fusionauth.api.service.moderation.cleanspeak.CleanSpeakClient;
import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.user.RegistrationFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationMultiFactorTrustPolicy;
import io.fusionauth.domain.CanonicalizationMethod;
import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.Integrations;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.MultiFactorLoginPolicy;
import io.fusionauth.domain.PasswordlessStrategy;
import io.fusionauth.domain.RefreshTokenExpirationPolicy;
import io.fusionauth.domain.RefreshTokenUsagePolicy;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.UnverifiedBehavior;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.EmailTemplateResponse;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.api.IPAccessControlListSearchRequest;
import io.fusionauth.domain.api.IPAccessControlListSearchResponse;
import io.fusionauth.domain.api.IntegrationResponse;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.api.LambdaResponse;
import io.fusionauth.domain.api.MessageTemplateResponse;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormType;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.message.sms.SMSMessageTemplate;
import io.fusionauth.domain.message.voice.VoiceMessageTemplate;
import io.fusionauth.domain.oauth2.ClientAuthenticationPolicy;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.oauth2.LogoutBehavior;
import io.fusionauth.domain.oauth2.OAuthApplicationRelationship;
import io.fusionauth.domain.oauth2.OAuthScopeConsentMode;
import io.fusionauth.domain.oauth2.OAuthScopeHandlingPolicy;
import io.fusionauth.domain.oauth2.Oauth2AuthorizedURLValidationPolicy;
import io.fusionauth.domain.oauth2.ProofKeyForCodeExchangePolicy;
import io.fusionauth.domain.oauth2.UnknownScopePolicy;
import io.fusionauth.domain.search.IPAccessControlListSearchCriteria;
import io.fusionauth.samlv2.domain.DigestAlgorithm;
import io.fusionauth.samlv2.domain.EncryptionAlgorithm;
import io.fusionauth.samlv2.domain.KeyLocation;
import io.fusionauth.samlv2.domain.KeyTransportAlgorithm;
import io.fusionauth.samlv2.domain.MaskGenerationFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

public abstract class BaseFormAction extends BaseAction {
  @FTLVariable
  public static final List<GrantType> availableGrants = Arrays.asList(new GrantType[] { GrantType.authorization_code, GrantType.device_code, GrantType.implicit, GrantType.password, GrantType.refresh_token });
  
  @FTLVariable
  public static final CanonicalizationMethod[] c14nMethods = CanonicalizationMethod.values();
  
  @FTLVariable
  public static final ClientAuthenticationPolicy[] clientAuthenticationPolicies = ClientAuthenticationPolicy.values();
  
  @FTLVariable
  public static final OAuthScopeConsentMode[] consentModes = OAuthScopeConsentMode.values();
  
  @FTLVariable
  public static final Application.RegistrationConfiguration.LoginIdType[] loginIdTypes = Application.RegistrationConfiguration.LoginIdType.values();
  
  @FTLVariable
  public static final LogoutBehavior[] logoutBehaviors = new LogoutBehavior[] { LogoutBehavior.AllApplications, LogoutBehavior.RedirectOnly };
  
  @FTLVariable
  public static final MultiFactorLoginPolicy[] multiFactorLoginPolicies = new MultiFactorLoginPolicy[] { MultiFactorLoginPolicy.Disabled, MultiFactorLoginPolicy.Enabled, MultiFactorLoginPolicy.ChallengeOnMediumRisk, MultiFactorLoginPolicy.ChallengeOnHighRisk, MultiFactorLoginPolicy.Required };
  
  @FTLVariable
  public static final ApplicationMultiFactorTrustPolicy[] multiFactorTrustPolicies = ApplicationMultiFactorTrustPolicy.values();
  
  @FTLVariable
  public static final OAuthApplicationRelationship[] oauthApplicationRelationships = OAuthApplicationRelationship.values();
  
  @FTLVariable
  public static final PasswordlessStrategy[] passwordlessStrategies = PasswordlessStrategy.values();
  
  @FTLVariable
  public static final ProofKeyForCodeExchangePolicy[] proofKeyForCodeExchangePolicies = ProofKeyForCodeExchangePolicy.values();
  
  @FTLVariable
  public static final RefreshTokenExpirationPolicy[] refreshTokenExpirationPolicies = RefreshTokenExpirationPolicy.values();
  
  @FTLVariable
  public static final RefreshTokenUsagePolicy[] refreshTokenUsagePolicies = RefreshTokenUsagePolicy.values();
  
  @FTLVariable
  public static final Application.RegistrationConfiguration.RegistrationType[] registrationTypes = Application.RegistrationConfiguration.RegistrationType.values();
  
  @FTLVariable
  public static final VerificationStrategy[] registrationVerificationStrategies = VerificationStrategy.values();
  
  @FTLVariable
  public static final EncryptionAlgorithm[] samlAssertionEncryptionAlgorithms = EncryptionAlgorithm.values();
  
  @FTLVariable
  public static final DigestAlgorithm[] samlAssertionEncryptionDigestAlgorithms = DigestAlgorithm.values();
  
  @FTLVariable
  public static final KeyLocation[] samlAssertionEncryptionKeyLocations = KeyLocation.values();
  
  @FTLVariable
  public static final KeyTransportAlgorithm[] samlAssertionEncryptionKeyTransportAlgorithms = KeyTransportAlgorithm.values();
  
  @FTLVariable
  public static final MaskGenerationFunction[] samlAssertionEncryptionMaskGenerationFunctions = MaskGenerationFunction.values();
  
  @FTLVariable
  public static final Application.SAMLv2Configuration.SAMLLogoutBehavior[] samlv2LogoutBehaviors = new Application.SAMLv2Configuration.SAMLLogoutBehavior[] { Application.SAMLv2Configuration.SAMLLogoutBehavior.AllParticipants, Application.SAMLv2Configuration.SAMLLogoutBehavior.OnlyOriginator };
  
  @FTLVariable
  public static final OAuthScopeHandlingPolicy[] scopeHandlingPolicies = OAuthScopeHandlingPolicy.values();
  
  @FTLVariable
  public static final RegistrationFrontendService.SelfRegistrationType[] selfRegistrationTypes = RegistrationFrontendService.SelfRegistrationType.values();
  
  @FTLVariable
  public static final Application.SAMLv2Configuration.XMLSignatureLocation[] signatureLocations = Application.SAMLv2Configuration.XMLSignatureLocation.values();
  
  @FTLVariable
  public static final UnknownScopePolicy[] unknownScopePolicies = UnknownScopePolicy.values();
  
  @FTLVariable
  public static final UnverifiedBehavior[] unverifiedRegistrationBehaviors = UnverifiedBehavior.values();
  
  @FTLVariable
  public final List<Form> adminRegistrationForms = new ArrayList<>();
  
  public final List<EmailTemplate> emailTemplates = new ArrayList<>();
  
  @FTLVariable
  public final List<IPAccessControlList> ipAccessControlLists = new ArrayList<>();
  
  public final List<Lambda> jwtLambdas = new ArrayList<>();
  
  public final List<Form> registrationForms = new ArrayList<>();
  
  public final List<Lambda> registrationLambdas = new ArrayList<>();
  
  public final List<Lambda> samlv2Lambdas = new ArrayList<>();
  
  @FTLVariable
  public final List<Form> selfServiceUserForms = new ArrayList<>();
  
  public final List<Lambda> userinfoLambdas = new ArrayList<>();
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  private final Cache<UUID, CachedTheme> themeCache;
  
  @FTLVariable
  public Key accessTokenKey;
  
  @FTLVariable
  public List<Key> accessTokenVerificationKeys = List.of();
  
  public Application application = new Application();
  
  public UUID applicationId;
  
  @FTLVariable
  public Oauth2AuthorizedURLValidationPolicy[] authorizedURLValidationPolicies = Oauth2AuthorizedURLValidationPolicy.values();
  
  public List<Application> cleanSpeakApplications;
  
  @FTLVariable
  public Key idTokenKey;
  
  @FTLVariable
  public List<Key> idTokenVerificationKeys = List.of();
  
  public Integrations integrations;
  
  @FTLVariable
  public List<Lambda> multiFactorRequirementLambdas = new ArrayList<>();
  
  @FTLVariable
  public List<Key> samlv2AuthnVerificationKeys = List.of();
  
  public List<Key> samlv2EncryptionKeys = new ArrayList<>();
  
  public List<Key> samlv2Keys = new ArrayList<>();
  
  @FTLVariable
  public List<Key> samlv2LogoutVerificationKeys = List.of();
  
  @FTLVariable
  public RegistrationFrontendService.SelfRegistrationType selfRegistrationType;
  
  @FTLVariable
  public List<SMSMessageTemplate> smsMessageTemplates = new ArrayList<>();
  
  public List<Theme> themes = new ArrayList<>();
  
  @FTLVariable
  public List<VoiceMessageTemplate> voiceMessageTemplates = new ArrayList<>();
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport, ProxyInfoSupplier paramProxyInfoSupplier, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport);
    this.proxyInfoSupplier = paramProxyInfoSupplier;
    this.themeCache = paramCache;
    paramFrontEndSupport.errorMapperFunction = (paramError -> paramError);
    paramFrontEndSupport.fieldMapperFunction = (paramString -> paramString.replaceAll("application\\.oauthConfiguration\\.authorizedOriginURLs\\[\\d+]", "application.oauthConfiguration.authorizedOriginURLs").replaceAll("application\\.oauthConfiguration\\.authorizedRedirectURLs\\[\\d+]", "application.oauthConfiguration.authorizedRedirectURLs").replaceAll("application\\.oauthConfiguration\\.authorizedResourceUris\\[\\d+]", "application.oauthConfiguration.authorizedResourceUris").replaceAll("application\\.samlv2Configuration\\.authorizedRedirectURLs\\[\\d+]", "application.samlv2Configuration.authorizedRedirectURLs"));
  }
  
  @PostParameterMethod
  public void postParameterSelfRegistrationType() {
    if (this.selfRegistrationType != null)
      switch (this.selfRegistrationType) {
        case Disabled:
          this.application.registrationConfiguration.enabled = false;
          this.application.registrationConfiguration.completeRegistration = false;
          break;
        case Complete:
          this.application.registrationConfiguration.enabled = false;
          this.application.registrationConfiguration.completeRegistration = true;
          break;
        case Create:
          this.application.registrationConfiguration.enabled = true;
          break;
      }  
  }
  
  @FormPrepareMethod
  public void prepare() {
    Tenant tenant;
    List list = (List)Objects.requireNonNullElseGet(((KeyResponse)superDelegate().execute(FusionAuthClient::retrieveKeys)).keys, Collections::emptyList);
    List<? extends Form> list1 = (List)Objects.requireNonNullElseGet(((FormResponse)superDelegate().execute(FusionAuthClient::retrieveForms)).forms, Collections::emptyList);
    this.registrationForms.addAll(list1);
    this.registrationForms.removeIf(paramForm -> (paramForm.type != FormType.registration));
    this.adminRegistrationForms.addAll(list1);
    this.adminRegistrationForms.removeIf(paramForm -> (paramForm.type != FormType.adminRegistration));
    this.selfServiceUserForms.addAll(list1);
    this.selfServiceUserForms.removeIf(paramForm -> (paramForm.type != FormType.selfServiceUser));
    if (this.application.registrationConfiguration.type == null)
      this.application.registrationConfiguration.type = Application.RegistrationConfiguration.RegistrationType.basic; 
    if (this.application.registrationConfiguration.enabled) {
      this.selfRegistrationType = RegistrationFrontendService.SelfRegistrationType.Create;
    } else if (this.application.registrationConfiguration.completeRegistration) {
      this.selfRegistrationType = RegistrationFrontendService.SelfRegistrationType.Complete;
    } else {
      this.selfRegistrationType = RegistrationFrontendService.SelfRegistrationType.Disabled;
    } 
    this.samlv2Keys.addAll(list.stream().filter(KeyValidator::validForSAMLSigning).toList());
    this.samlv2EncryptionKeys.addAll(list.stream().filter(KeyValidator::validForSAMLEncryption).toList());
    this.emailTemplates.addAll((Collection<? extends EmailTemplate>)Objects.requireNonNullElseGet(((EmailTemplateResponse)superDelegate().execute(FusionAuthClient::retrieveEmailTemplates)).emailTemplates, Collections::emptyList));
    List<MessageTemplate> list2 = ((MessageTemplateResponse)superDelegate().execute(FusionAuthClient::retrieveMessageTemplates)).messageTemplates;
    if (list2 != null) {
      list2.stream().filter(paramMessageTemplate -> (paramMessageTemplate.getType() == MessageType.SMS)).forEach(paramMessageTemplate -> this.smsMessageTemplates.add((SMSMessageTemplate)paramMessageTemplate));
      list2.stream().filter(paramMessageTemplate -> (paramMessageTemplate.getType() == MessageType.Voice)).forEach(paramMessageTemplate -> this.voiceMessageTemplates.add((VoiceMessageTemplate)paramMessageTemplate));
    } 
    this.jwtLambdas.addAll((Collection<? extends Lambda>)Objects.requireNonNullElseGet(((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambdasByType(LambdaType.JWTPopulate))).lambdas, Collections::emptyList));
    this.multiFactorRequirementLambdas.addAll((Collection<? extends Lambda>)Objects.requireNonNullElseGet(((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambdasByType(LambdaType.MFARequirement))).lambdas, Collections::emptyList));
    this.samlv2Lambdas.addAll((Collection<? extends Lambda>)Objects.requireNonNullElseGet(((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambdasByType(LambdaType.SAMLv2Populate))).lambdas, Collections::emptyList));
    this.registrationLambdas.addAll((Collection<? extends Lambda>)Objects.requireNonNullElseGet(((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambdasByType(LambdaType.SelfServiceRegistrationValidation))).lambdas, Collections::emptyList));
    this.userinfoLambdas.addAll((Collection<? extends Lambda>)Objects.requireNonNullElseGet(((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambdasByType(LambdaType.UserInfoPopulate))).lambdas, Collections::emptyList));
    this.integrations = ((IntegrationResponse)superDelegate().execute(FusionAuthClient::retrieveIntegration)).integrations;
    if (this.integrations.cleanspeak.enabled) {
      CleanSpeakClient cleanSpeakClient = new CleanSpeakClient(this.integrations.cleanspeak.apiKey, this.integrations.cleanspeak.url.toString(), paramClientResponse -> paramClientResponse.successResponse, paramClientResponse -> {
          
          }this.proxyInfoSupplier);
      ClientResponse<ApplicationResponse, Errors> clientResponse = cleanSpeakClient.retrieveApplications();
      if (clientResponse.wasSuccessful())
        this.cleanSpeakApplications = ((ApplicationResponse)clientResponse.successResponse).applications; 
    } 
    this.themes.addAll(this.themeCache.getAll().stream().sorted((paramCachedTheme1, paramCachedTheme2) -> paramCachedTheme2.insertInstant.compareTo(paramCachedTheme1.insertInstant)).toList());
    if (this.application.tenantId != null) {
      tenant = this.frontEndSupport.tenantReader.retrieveById(this.application.tenantId);
    } else {
      tenant = this.frontEndSupport.tenantReader.retrieveById(this.frontEndSupport.fusionAuthTenantId);
    } 
    if (this.application.jwtConfiguration.timeToLiveInSeconds == 0)
      this.application.jwtConfiguration.timeToLiveInSeconds = tenant.jwtConfiguration.timeToLiveInSeconds; 
    if (this.application.jwtConfiguration.refreshTokenTimeToLiveInMinutes == 0)
      this.application.jwtConfiguration.refreshTokenTimeToLiveInMinutes = tenant.jwtConfiguration.refreshTokenTimeToLiveInMinutes; 
    if (this.application.jwtConfiguration.accessTokenKeyId == null)
      this.application.jwtConfiguration.accessTokenKeyId = tenant.jwtConfiguration.accessTokenKeyId; 
    if (this.application.jwtConfiguration.idTokenKeyId == null)
      this.application.jwtConfiguration.idTokenKeyId = tenant.jwtConfiguration.idTokenKeyId; 
    if (this.application.jwtConfiguration.accessTokenKeyId != null)
      fetchKey(this.application.jwtConfiguration.accessTokenKeyId).ifPresent(paramKey -> this.accessTokenKey = paramKey); 
    if (this.application.jwtConfiguration.idTokenKeyId != null)
      fetchKey(this.application.jwtConfiguration.idTokenKeyId).ifPresent(paramKey -> this.idTokenKey = paramKey); 
    if (this.application.jwtConfiguration.refreshTokenExpirationPolicy == null)
      this.application.jwtConfiguration.refreshTokenExpirationPolicy = tenant.jwtConfiguration.refreshTokenExpirationPolicy; 
    if (this.application.jwtConfiguration.refreshTokenUsagePolicy == null) {
      this.application.jwtConfiguration.refreshTokenUsagePolicy = tenant.jwtConfiguration.refreshTokenUsagePolicy;
      this.application.jwtConfiguration.refreshTokenRevocationPolicy = tenant.jwtConfiguration.refreshTokenRevocationPolicy;
    } 
    this


      
      .accessTokenVerificationKeys = (List<Key>)this.application.jwtConfiguration.accessTokenVerificationKeyIds.stream().map(this::fetchKey).flatMap(Optional::stream).collect(Collectors.toList());
    this


      
      .idTokenVerificationKeys = (List<Key>)this.application.jwtConfiguration.idTokenVerificationKeyIds.stream().map(this::fetchKey).flatMap(Optional::stream).collect(Collectors.toList());
    this


      
      .samlv2AuthnVerificationKeys = (List<Key>)this.application.samlv2Configuration.verificationKeyIds.stream().map(this::fetchKey).flatMap(Optional::stream).collect(Collectors.toList());
    this


      
      .samlv2LogoutVerificationKeys = (List<Key>)this.application.samlv2Configuration.logout.verificationKeyIds.stream().map(this::fetchKey).flatMap(Optional::stream).collect(Collectors.toList());
    if (this.application.oauthConfiguration.clientId == null) {
      this.application.oauthConfiguration.enabledGrants.add(GrantType.authorization_code);
      this.application.oauthConfiguration.enabledGrants.add(GrantType.refresh_token);
      this.application.oauthConfiguration.generateRefreshTokens = true;
    } 
    if (this.frontEndSupport.isGET() && this.application.registrationDeletePolicy.unverified.numberOfDaysToRetain == 0)
      this.application.registrationDeletePolicy.unverified.numberOfDaysToRetain = 30; 
    if (this.application.formConfiguration.adminRegistrationFormId == null) {
      Application application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(Application.FUSIONAUTH_APP_ID))).application;
      this.application.formConfiguration.adminRegistrationFormId = application.formConfiguration.adminRegistrationFormId;
    } 
    if (this.frontEndSupport.isGET() && this.application.samlv2Configuration.logout.behavior == null)
      this.application.samlv2Configuration.logout.behavior = Application.SAMLv2Configuration.SAMLLogoutBehavior.AllParticipants; 
    if (this.frontEndSupport.isGET() && this.application.unverified.behavior == null)
      this.application.unverified.behavior = UnverifiedBehavior.Allow; 
    this.ipAccessControlLists.addAll((Collection<? extends IPAccessControlList>)Objects.requireNonNullElseGet(((IPAccessControlListSearchResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchIPAccessControlLists(new IPAccessControlListSearchRequest((new IPAccessControlListSearchCriteria()).with(()))))).ipAccessControlLists, List::of));
  }
  
  private Optional<Key> fetchKey(UUID paramUUID) {
    ClientResponse<KeyResponse, Errors> clientResponse = this.superClient.retrieveKey(paramUUID);
    if (clientResponse.wasSuccessful())
      return Optional.of(((KeyResponse)clientResponse.successResponse).key); 
    return Optional.empty();
  }
}
