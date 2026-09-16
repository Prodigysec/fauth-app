package io.fusionauth.app.action.ajax.tenant.connectorPolicy;

import com.google.inject.Inject;
import com.inversoft.util.CollectionTools;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.ConnectorResponse;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "tenant_manager"})
public class AddAction extends BaseAJAXAction {
  @FTLVariable
  public String connectorDomains;
  
  public UUID connectorId;
  
  @FTLVariable
  public boolean connectorMigrate;
  
  @FTLVariable
  public List<BaseConnectorConfiguration> connectors = new ArrayList<>();
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    return "success";
  }
  
  @FormPrepareMethod
  public void prepare() {
    this.connectors.addAll(((ConnectorResponse)superDelegate().execute(FusionAuthClient::retrieveConnectors)).connectors);
    this.connectors.removeIf(paramBaseConnectorConfiguration -> paramBaseConnectorConfiguration.id.equals(BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID));
  }
  
  @ValidationMethod
  public void validate() {
    Collection collection = CollectionTools.stringToCollection(this.connectorDomains);
    if (collection.isEmpty())
      this.frontEndSupport.addFieldError("connectorDomains", "[empty]connectorDomains", new Object[0]); 
    if (this.connectorId == null)
      this.frontEndSupport.addFieldError("connectorId", "[missing]connectorId", new Object[0]); 
  }
}
