package io.fusionauth.app.action.ajax.tenant;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.security.PasswordEncryptorLibrary;
import io.fusionauth.app.action.admin.tenant.IndexAction;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.api.ConnectorResponse;
import io.fusionauth.domain.api.EmailTemplateResponse;
import io.fusionauth.domain.api.MessageTemplateResponse;
import io.fusionauth.domain.api.MessengerResponse;
import io.fusionauth.domain.api.TenantResponse;
import io.fusionauth.domain.api.ThemeResponse;
import io.fusionauth.domain.connector.ConnectorPolicy;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.annotation.AlternateMessageResources;

@Action(value = "{tenantId}", requiresAuthentication = true, constraints = {"admin", "tenant_manager"})
@AlternateMessageResources(actions = {IndexAction.class})
public class ViewAction extends BaseAJAXAction {
  @FTLVariable
  public static final List<EventType> eventTypes = EventType.allTypes();
  
  private final PasswordEncryptorLibrary passwordEncryptorLibrary;
  
  @FTLVariable
  public Map<String, String> connectorNames = new LinkedHashMap<>();
  
  @FTLVariable
  public Map<String, String> emailTemplateNames = new LinkedHashMap<>();
  
  public String encryptionSchemeDisplayName;
  
  @FTLVariable
  public Map<String, String> messageTemplateNames = new LinkedHashMap<>();
  
  @FTLVariable
  public Map<String, String> messengerNames = new LinkedHashMap<>();
  
  public Tenant tenant;
  
  public Theme theme;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport, PasswordEncryptorLibrary paramPasswordEncryptorLibrary) {
    super(paramFrontEndSupport);
    this.passwordEncryptorLibrary = paramPasswordEncryptorLibrary;
  }
  
  public String get() {
    this.tenant = ((TenantResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTenant(this.tenantId))).tenant;
    this.theme = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme(this.tenant.themeId))).theme;
    this



      
      .messageTemplateNames = (Map<String, String>)Stream.<UUID>of(new UUID[] { this.tenant.phoneConfiguration.verificationCompleteTemplateId, this.tenant.phoneConfiguration.verificationTemplateId }).filter(Objects::nonNull).distinct().map(paramUUID -> ((MessageTemplateResponse)superDelegate().execute(())).messageTemplate).collect(Collectors.toMap(paramMessageTemplate -> paramMessageTemplate.id.toString(), paramMessageTemplate -> paramMessageTemplate.name));
    this



      
      .emailTemplateNames = (Map<String, String>)Stream.<UUID>of(new UUID[] { this.tenant.emailConfiguration.verificationEmailTemplateId, this.tenant.emailConfiguration.emailVerifiedEmailTemplateId }).filter(Objects::nonNull).distinct().map(paramUUID -> ((EmailTemplateResponse)superDelegate().execute(())).emailTemplate).collect(Collectors.toMap(paramEmailTemplate -> paramEmailTemplate.id.toString(), paramEmailTemplate -> paramEmailTemplate.name));
    this

      
      .messengerNames = (Map<String, String>)Stream.<UUID>of(this.tenant.phoneConfiguration.messengerId).filter(Objects::nonNull).map(paramUUID -> ((MessengerResponse)superDelegate().execute(())).messenger).collect(Collectors.toMap(paramBaseMessengerConfiguration -> paramBaseMessengerConfiguration.id.toString(), paramBaseMessengerConfiguration -> paramBaseMessengerConfiguration.name));
    for (Iterator<ConnectorPolicy> iterator = this.tenant.connectorPolicies.iterator(); iterator.hasNext(); ) {
      ConnectorPolicy connectorPolicy = iterator.next();
      String str = ((ConnectorResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveConnector(paramConnectorPolicy.connectorId))).connector.name;
      this.connectorNames.put(connectorPolicy.connectorId.toString(), str);
    } 
    this.encryptionSchemeDisplayName = this.frontEndSupport.messageProvider.getOptionalMessage(this.tenant.passwordEncryptionConfiguration.encryptionScheme, new Object[0]);
    if (this.encryptionSchemeDisplayName == null)
      this.encryptionSchemeDisplayName = this.passwordEncryptorLibrary.getEncryptorDisplayName(this.tenant.passwordEncryptionConfiguration.encryptionScheme); 
    return "render";
  }
}
