package io.fusionauth.app.action.admin.connector;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.ConnectorRequest;
import io.fusionauth.domain.api.ConnectorResponse;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorType;
import io.fusionauth.domain.connector.GenericConnectorConfiguration;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(value = "{type}", requiresAuthentication = true, constraints = {"admin", "connector_manager"})
@List({@Redirect(code = "success", uri = "/admin/connector/"), @Redirect(code = "api-error", uri = "/admin/connector/"), @Redirect(code = "unsupported-connector-type", uri = "/admin/connector/")})
public class AddAction extends BaseFormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    if (this.connector.getType() == ConnectorType.Generic)
      ((GenericConnectorConfiguration)this.connector).headers.putAll(ActionTools.keyValueCollectionsToMap(this.headerNames, this.headerValues)); 
    BaseConnectorConfiguration baseConnectorConfiguration = ((ConnectorResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createConnector(this.connectorId, new ConnectorRequest(this.connector)))).connector;
    writeAuditLog("Created the connector with Id [" + String.valueOf(baseConnectorConfiguration.id) + "] and name [" + this.connector.name + "]");
    return "success";
  }
}
