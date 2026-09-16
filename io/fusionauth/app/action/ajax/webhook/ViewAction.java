package io.fusionauth.app.action.ajax.webhook;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.api.WebhookResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{webhookId}", requiresAuthentication = true, constraints = {"admin", "webhook_manager"})
public class ViewAction extends BaseAJAXAction {
  public Key signingKey;
  
  public Key sslCertificateKey;
  
  public Webhook webhook;
  
  public UUID webhookId;
  
  public List<Tenant> webhookTenants = new ArrayList<>();
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.webhook = ((WebhookResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebhook(this.webhookId))).webhook;
    if (this.webhook.sslCertificateKeyId != null)
      this.sslCertificateKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.webhook.sslCertificateKeyId))).key; 
    this.webhookTenants.addAll((Collection<? extends Tenant>)this.tenants.values().stream().filter(paramTenant -> this.webhook.tenantIds.contains(paramTenant.id)).collect(Collectors.toList()));
    this.webhookTenants.sort(Comparator.comparing(paramTenant -> paramTenant.name));
    if (this.webhook.signatureConfiguration.signingKeyId != null)
      this.signingKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.webhook.signatureConfiguration.signingKeyId))).key; 
    return "render";
  }
}
