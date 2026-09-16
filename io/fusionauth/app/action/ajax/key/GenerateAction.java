package io.fusionauth.app.action.ajax.key;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.KeyRequest;
import io.fusionauth.domain.api.KeyResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

@Action(value = "{type}", requiresAuthentication = true, constraints = {"admin", "key_manager"})
public class GenerateAction extends BaseFormAction {
  private final UUID defaultTenantId;
  
  @FTLVariable
  public String defaultIssuer;
  
  @Inject
  public GenerateAction(FrontEndSupport paramFrontEndSupport, @FusionAuthTenantId UUID paramUUID) {
    super(paramFrontEndSupport);
    this.defaultTenantId = paramUUID;
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    Key key = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.generateKey(this.keyId, new KeyRequest(this.key)))).key;
    writeAuditLog("Generated new key with Id [" + String.valueOf(key.id) + "]");
    return "success";
  }
  
  @FormPrepareMethod
  public void prepareForm() {
    this.defaultIssuer = ((Tenant)this.tenants.get(this.defaultTenantId)).issuer;
  }
}
