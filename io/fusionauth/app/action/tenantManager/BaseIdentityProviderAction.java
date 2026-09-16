package io.fusionauth.app.action.tenantManager;

import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.tenantManager.TenantManagerService;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.api.KeyRequest;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.DomainBasedIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.LoginHintConfiguration;
import io.fusionauth.domain.provider.OpenIdConnectApplicationConfiguration;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2ApplicationConfiguration;
import io.fusionauth.domain.provider.SAMLv2AssertionConfiguration;
import io.fusionauth.domain.provider.SAMLv2AssertionDecryptionConfiguration;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import io.fusionauth.domain.tenantManager.TenantManagerApplicationConfiguration;
import io.fusionauth.domain.tenantManager.TenantManagerConfiguration;
import io.fusionauth.domain.tenantManager.TenantManagerIdentityProviderTypeConfiguration;
import io.fusionauth.pem.domain.PEM;
import io.fusionauth.samlv2.domain.NameIDFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.validation.ValidationMethod;

@List({@Redirect(code = "success", uri = "/tenant-manager/sso/?tenantId=${tenantId}"), @Redirect(code = "missing", uri = "/tenant-manager/sso/?tenantId=${tenantId}"), @Redirect(code = "test", uri = "/tenant-manager/sso/test/start/${identityProviderId}?tenantId=${tenantId}")})
public class BaseIdentityProviderAction extends BaseTenantManagerAction {
  @FTLVariable
  public String brandName;
  
  @FTLVariable
  public String domains;
  
  @FTLVariable
  public Map<String, String> formFieldMappings = new HashMap<>();
  
  @FTLVariable
  public BaseIdentityProvider<?> identityProvider = new OpenIdConnectIdentityProvider();
  
  public UUID identityProviderId;
  
  public String otherNameIdFormat;
  
  @FTLVariable
  public List<FormField> registrationFormFields = new ArrayList<>();
  
  public boolean testConfiguration;
  
  @PreParameter
  public IdentityProviderType type;
  
  @FTLVariable
  public TenantManagerIdentityProviderTypeConfiguration typeConfiguration = new TenantManagerIdentityProviderTypeConfiguration();
  
  public boolean updateVerificationKey;
  
  public boolean useOpenIdDiscovery;
  
  @FTLVariable
  public String verificationKey;
  
  public BaseIdentityProviderAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  @PreRenderMethod
  public void formPrepare() {
    this.brandName = this.tenantManagerConfiguration.brandName;
    if (this.identityProviderId == null)
      this.identityProvider.linkingStrategy = ((TenantManagerIdentityProviderTypeConfiguration)this.tenantManagerConfiguration.identityProviderTypeConfigurations.get(this.type.name())).linkingStrategy; 
    this.identityProvider.source = "Tenant Manager";
    this.registrationFormFields = getFormFields(this.tenantManagerConfiguration);
    this.typeConfiguration = this.tenantManagerConfiguration.identityProviderTypeConfigurations.get(this.type.name());
    if (IdentityProviderType.SAMLv2.equals(this.type) && this.identityProviderId != null) {
      SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)this.identityProvider;
      if (((SAMLv2IdentityProvider)this.identityProvider).keyId != null) {
        ClientResponse<KeyResponse, Errors> clientResponse = this.superClient.retrieveKey(sAMLv2IdentityProvider.keyId);
        if (!clientResponse.wasSuccessful() || clientResponse.successResponse == null || ((KeyResponse)clientResponse.successResponse).key == null) {
          this.frontEndSupport.addGeneralError("[missing]verificationKey", new Object[0]);
          return;
        } 
        this.verificationKey = (((KeyResponse)clientResponse.successResponse).key.certificate != null) ? ((KeyResponse)clientResponse.successResponse).key.certificate : ((KeyResponse)clientResponse.successResponse).key.publicKey;
      } 
      if (isCustomNameIdFormatValue(sAMLv2IdentityProvider.nameIdFormat)) {
        this.otherNameIdFormat = sAMLv2IdentityProvider.nameIdFormat;
        sAMLv2IdentityProvider.nameIdFormat = "other";
      } 
    } 
    populateAttributeMappings();
  }
  
  @PreParameterMethod
  public void setupIdentityProvider() {
    switch (this.type) {
      case OpenIDConnect:
        this.identityProvider = new OpenIdConnectIdentityProvider();
        break;
      case SAMLv2:
        this.identityProvider = new SAMLv2IdentityProvider();
        break;
      default:
        throw new ErrorException("[invalid]");
    } 
    if (this.type == IdentityProviderType.SAMLv2) {
      SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)this.identityProvider;
      if (!NameIDFormat.EmailAddress.toSAMLFormat().equals(sAMLv2IdentityProvider.nameIdFormat) && !NameIDFormat.Persistent.toSAMLFormat().equals(sAMLv2IdentityProvider.nameIdFormat)) {
        this.otherNameIdFormat = sAMLv2IdentityProvider.nameIdFormat;
        sAMLv2IdentityProvider.nameIdFormat = "";
      } 
    } 
  }
  
  @ValidationMethod
  public void validate() {
    if (this.type == null || (this.type != IdentityProviderType.OpenIDConnect && this.type != IdentityProviderType.SAMLv2))
      throw new NotFoundException(); 
    if (this.type == IdentityProviderType.SAMLv2 && 
      "other".equals(((SAMLv2IdentityProvider)this.identityProvider).nameIdFormat) && (this.otherNameIdFormat == null || this.otherNameIdFormat
      .isBlank()))
      this.frontEndSupport.addFieldError("otherNameIdFormat", "[missing]otherNameIdFormat", new Object[0]); 
  }
  
  protected void cleanupKeys(List<UUID> paramList) {
    for (UUID uUID : paramList)
      this.superClient.deleteKey(uUID); 
  }
  
  protected void configureIdentityProviderApplications(TenantManagerConfiguration paramTenantManagerConfiguration) {
    paramTenantManagerConfiguration.applicationConfigurations.forEach(paramTenantManagerApplicationConfiguration -> {
          OpenIdConnectApplicationConfiguration openIdConnectApplicationConfiguration;
          SAMLv2ApplicationConfiguration sAMLv2ApplicationConfiguration;
          switch (this.type) {
            case OpenIDConnect:
              openIdConnectApplicationConfiguration = (new OpenIdConnectApplicationConfiguration()).with(());
              ((OpenIdConnectIdentityProvider)this.identityProvider).applicationConfiguration.put(paramTenantManagerApplicationConfiguration.applicationId, openIdConnectApplicationConfiguration);
              break;
            case SAMLv2:
              sAMLv2ApplicationConfiguration = (new SAMLv2ApplicationConfiguration()).with(());
              ((SAMLv2IdentityProvider)this.identityProvider).applicationConfiguration.put(paramTenantManagerApplicationConfiguration.applicationId, sAMLv2ApplicationConfiguration);
              break;
          } 
        });
  }
  
  protected void populateAttributeMappings() {
    this.registrationFormFields.forEach(paramFormField -> {
          this.formFieldMappings.put(paramFormField.key, paramFormField.name);
          if (this.identityProvider.attributeMappings.get(paramFormField.key) == null)
            if (this.typeConfiguration.defaultAttributeMappings.get(paramFormField.key) != null) {
              this.identityProvider.attributeMappings.put(paramFormField.key, this.typeConfiguration.defaultAttributeMappings.get(paramFormField.key));
            } else {
              this.identityProvider.attributeMappings.put(paramFormField.key, "");
            }  
        });
  }
  
  protected List<UUID> postInitialize() {
    return postInitialize((BaseIdentityProvider<?>)null);
  }
  
  protected List<UUID> postInitialize(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    ArrayList<UUID> arrayList = new ArrayList();
    String str = UUID.randomUUID().toString().substring(0, 8);
    setTenantManagerDefaults(this.tenantManagerConfiguration, paramBaseIdentityProvider);
    this.identityProvider.applicationConfiguration.clear();
    configureIdentityProviderApplications(this.tenantManagerConfiguration);
    if (IdentityProviderType.SAMLv2.equals(this.type)) {
      SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)this.identityProvider;
      if (paramBaseIdentityProvider == null) {
        sAMLv2IdentityProvider.keyId = importVerification(this.verificationKey, str);
        if (sAMLv2IdentityProvider.keyId == null)
          return arrayList; 
        arrayList.add(sAMLv2IdentityProvider.keyId);
        if (sAMLv2IdentityProvider.signRequest) {
          sAMLv2IdentityProvider.requestSigningKeyId = generateSigningKey(str);
          if (sAMLv2IdentityProvider.requestSigningKeyId == null)
            return arrayList; 
          arrayList.add(sAMLv2IdentityProvider.requestSigningKeyId);
        } 
        if (this.otherNameIdFormat != null)
          sAMLv2IdentityProvider.nameIdFormat = this.otherNameIdFormat; 
      } else {
        if (this.updateVerificationKey) {
          Key key = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(((SAMLv2IdentityProvider)paramBaseIdentityProvider).keyId))).key;
          String str1 = (key.certificate != null) ? key.certificate : key.publicKey;
          if (!str1.equals(this.verificationKey)) {
            sAMLv2IdentityProvider.keyId = importVerification(this.verificationKey, str);
            if (sAMLv2IdentityProvider.keyId == null)
              return arrayList; 
            arrayList.add(sAMLv2IdentityProvider.keyId);
          } else {
            sAMLv2IdentityProvider.keyId = ((SAMLv2IdentityProvider)paramBaseIdentityProvider).keyId;
          } 
        } else {
          sAMLv2IdentityProvider.keyId = ((SAMLv2IdentityProvider)paramBaseIdentityProvider).keyId;
        } 
        if (sAMLv2IdentityProvider.signRequest) {
          if (!((SAMLv2IdentityProvider)paramBaseIdentityProvider).signRequest) {
            sAMLv2IdentityProvider.requestSigningKeyId = generateSigningKey(str);
            if (sAMLv2IdentityProvider.requestSigningKeyId == null)
              return arrayList; 
            arrayList.add(sAMLv2IdentityProvider.requestSigningKeyId);
          } else {
            sAMLv2IdentityProvider.requestSigningKeyId = ((SAMLv2IdentityProvider)paramBaseIdentityProvider).requestSigningKeyId;
          } 
        } else {
          sAMLv2IdentityProvider.requestSigningKeyId = null;
        } 
        if (!isBuiltInNameIdFormat(sAMLv2IdentityProvider.nameIdFormat))
          sAMLv2IdentityProvider.nameIdFormat = this.otherNameIdFormat; 
      } 
    } 
    return arrayList;
  }
  
  protected void setTenantManagerDefaults(TenantManagerConfiguration paramTenantManagerConfiguration, BaseIdentityProvider<?> paramBaseIdentityProvider) {
    OpenIdConnectIdentityProvider openIdConnectIdentityProvider;
    SAMLv2IdentityProvider sAMLv2IdentityProvider;
    this.identityProvider.source = "Tenant Manager";
    this.identityProvider.tenantId = this.tenantId;
    if (paramBaseIdentityProvider != null) {
      this.identityProvider.data.clear();
      this.identityProvider.data.putAll(paramBaseIdentityProvider.data);
      this.identityProvider.linkingStrategy = paramBaseIdentityProvider.linkingStrategy;
    } else {
      this.identityProvider.linkingStrategy = ((TenantManagerIdentityProviderTypeConfiguration)paramTenantManagerConfiguration.identityProviderTypeConfigurations.get(this.type.name())).linkingStrategy;
    } 
    Collection<? extends String> collection = CollectionTools.stringToCollection(this.domains);
    if (this.identityProvider instanceof DomainBasedIdentityProvider)
      ((DomainBasedIdentityProvider)this.identityProvider).getDomains().addAll(collection); 
    switch (this.type) {
      case OpenIDConnect:
        openIdConnectIdentityProvider = (OpenIdConnectIdentityProvider)this.identityProvider;
        if (paramBaseIdentityProvider != null) {
          OpenIdConnectIdentityProvider openIdConnectIdentityProvider1 = (OpenIdConnectIdentityProvider)paramBaseIdentityProvider;
          openIdConnectIdentityProvider.postRequest = openIdConnectIdentityProvider1.postRequest;
          openIdConnectIdentityProvider.debug = openIdConnectIdentityProvider1.debug;
          openIdConnectIdentityProvider.lambdaConfiguration = openIdConnectIdentityProvider1.lambdaConfiguration;
        } 
        if (!this.useOpenIdDiscovery) {
          openIdConnectIdentityProvider.oauth2.issuer = null;
          break;
        } 
        openIdConnectIdentityProvider.oauth2.authorization_endpoint = null;
        openIdConnectIdentityProvider.oauth2.token_endpoint = null;
        openIdConnectIdentityProvider.oauth2.userinfo_endpoint = null;
        break;
      case SAMLv2:
        sAMLv2IdentityProvider = (SAMLv2IdentityProvider)this.identityProvider;
        if (paramBaseIdentityProvider != null) {
          SAMLv2IdentityProvider sAMLv2IdentityProvider1 = (SAMLv2IdentityProvider)paramBaseIdentityProvider;
          sAMLv2IdentityProvider.postRequest = sAMLv2IdentityProvider1.postRequest;
          sAMLv2IdentityProvider.debug = sAMLv2IdentityProvider1.debug;
          sAMLv2IdentityProvider.lambdaConfiguration = sAMLv2IdentityProvider1.lambdaConfiguration;
          sAMLv2IdentityProvider.uniqueIdClaim = sAMLv2IdentityProvider1.uniqueIdClaim;
          sAMLv2IdentityProvider.emailClaim = sAMLv2IdentityProvider1.emailClaim;
          sAMLv2IdentityProvider.useNameIdForEmail = sAMLv2IdentityProvider1.useNameIdForEmail;
          sAMLv2IdentityProvider.usernameClaim = sAMLv2IdentityProvider1.usernameClaim;
          sAMLv2IdentityProvider.xmlSignatureC14nMethod = sAMLv2IdentityProvider1.xmlSignatureC14nMethod;
          sAMLv2IdentityProvider.assertionConfiguration = new SAMLv2AssertionConfiguration(sAMLv2IdentityProvider1.assertionConfiguration);
          sAMLv2IdentityProvider.assertionDecryptionConfiguration = new SAMLv2AssertionDecryptionConfiguration(sAMLv2IdentityProvider1.assertionDecryptionConfiguration);
          sAMLv2IdentityProvider.buttonImageURL = sAMLv2IdentityProvider1.buttonImageURL;
          sAMLv2IdentityProvider.loginHintConfiguration = new LoginHintConfiguration(sAMLv2IdentityProvider1.loginHintConfiguration);
        } 
        sAMLv2IdentityProvider.idpInitiatedConfiguration.enabled = false;
        break;
    } 
    this.registrationFormFields = getFormFields(paramTenantManagerConfiguration);
    Set<?> set = (Set)this.registrationFormFields.stream().map(paramFormField -> paramFormField.key).collect(Collectors.toSet());
    this.identityProvider.attributeMappings.keySet().retainAll(set);
    set.forEach(paramString -> {
          if (!this.identityProvider.attributeMappings.containsKey(paramString) || this.identityProvider.attributeMappings.get(paramString) == null)
            this.identityProvider.attributeMappings.put(paramString, ""); 
        });
  }
  
  private UUID generateSigningKey(String paramString) {
    KeyRequest keyRequest = new KeyRequest((new Key()).with(paramKey -> {
            paramKey.name = "Tenant Manager IdP Signing Key " + paramString;
            paramKey.algorithm = Key.KeyAlgorithm.RS256;
            paramKey.length = Integer.valueOf(2048);
          }));
    ClientResponse<KeyResponse, Errors> clientResponse = this.superClient.generateKey(null, keyRequest);
    if (!clientResponse.wasSuccessful() || clientResponse.successResponse == null || ((KeyResponse)clientResponse.successResponse).key == null) {
      this.frontEndSupport.addGeneralError("[error]signRequest", new Object[0]);
      return null;
    } 
    return ((KeyResponse)clientResponse.successResponse).key.id;
  }
  
  private List<FormField> getFormFields(TenantManagerConfiguration paramTenantManagerConfiguration) {
    if (paramTenantManagerConfiguration.attributeFormId == null)
      return List.of(); 
    ClientResponse<FormResponse, Void> clientResponse = this.superClient.retrieveForm(paramTenantManagerConfiguration.attributeFormId);
    if (!clientResponse.wasSuccessful() || clientResponse.successResponse == null || ((FormResponse)clientResponse.successResponse).form == null)
      return List.of(); 
    Form form = ((FormResponse)clientResponse.successResponse).form;
    if (form.steps == null)
      return List.of(); 
    return form.steps
      .stream()
      .flatMap(paramFormStep -> paramFormStep.fields.stream().map(()).filter(()).map(()))



      
      .toList();
  }
  
  private UUID importVerification(String paramString1, String paramString2) {
    Key key = (new Key()).with(paramKey -> {
          paramKey.name = "Tenant Manager IdP " + paramString1;
          if (isCertificatePEM(paramString2)) {
            paramKey.certificate = paramString2;
          } else {
            paramKey.publicKey = paramString2;
          } 
        });
    ClientResponse<KeyResponse, Errors> clientResponse = this.superClient.importKey(null, new KeyRequest(key));
    if (!clientResponse.wasSuccessful() || clientResponse.successResponse == null || ((KeyResponse)clientResponse.successResponse).key == null) {
      this.frontEndSupport.addFieldError("verificationKey", "[invalid]verificationKey", new Object[0]);
      return null;
    } 
    return ((KeyResponse)clientResponse.successResponse).key.id;
  }
  
  private boolean isBuiltInNameIdFormat(String paramString) {
    return (NameIDFormat.EmailAddress.toSAMLFormat().equals(paramString) || NameIDFormat.Persistent
      .toSAMLFormat().equals(paramString));
  }
  
  private boolean isCertificatePEM(String paramString) {
    try {
      PEM pEM = PEM.decode(paramString);
      return (pEM.certificate != null);
    } catch (Exception exception) {
      return false;
    } 
  }
  
  private boolean isCustomNameIdFormatValue(String paramString) {
    return (paramString != null && !paramString.isBlank() && !"other".equals(paramString) && !isBuiltInNameIdFormat(paramString));
  }
}
