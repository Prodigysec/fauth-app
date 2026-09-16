package io.fusionauth.app.action.admin.webhook;

import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.event.EventType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

public abstract class BaseFormAction extends BaseAction {
  @FTLVariable
  public static final List<EventType> eventTypes = EventType.allTypes();
  
  public final List<Key> sslKeys = new ArrayList<>();
  
  public List<UUID> applicationIds = new ArrayList<>();
  
  public List<Application> applications;
  
  public List<String> headerNames = new ArrayList<>();
  
  public List<String> headerValues = new ArrayList<>();
  
  public List<Key> signingKeys = new ArrayList<>();
  
  public Webhook webhook = new Webhook();
  
  public UUID webhookId;
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @PostParameterMethod
  public void postParameter() {
    if (this.frontEndSupport.isPOST())
      if (this.webhook.sslCertificateKeyId != null)
        this.webhook.sslCertificate = null;  
  }
  
  @FormPrepareMethod
  public void prepare() {
    this.applications = ((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications;
    this.applications.sort(Comparator.comparing(paramApplication -> paramApplication.name));
    List list = (List)Objects.requireNonNullElseGet(((KeyResponse)superDelegate().execute(FusionAuthClient::retrieveKeys)).keys, Collections::emptyList);
    this.sslKeys.addAll(list.stream().filter(KeyValidator::validCertificate).toList());
    this.signingKeys.addAll(list.stream().filter(KeyValidator::validForWebhookSigning).toList());
    if (this.tenants.size() > 1)
      this.applications.forEach(paramApplication -> {
            if (paramApplication.tenantId != null) {
              Tenant tenant = this.tenants.get(paramApplication.tenantId);
              paramApplication.name = "%s (%s)".formatted(new Object[] { paramApplication.name, tenant.name });
            } else {
              paramApplication.name = "%s".formatted(new Object[] { paramApplication.name });
            } 
          }); 
  }
}
