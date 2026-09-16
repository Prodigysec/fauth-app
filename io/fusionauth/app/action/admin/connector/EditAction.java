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
import io.fusionauth.domain.connector.LDAPConnectorConfiguration;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(value = "{type}/{connectorId}", requiresAuthentication = true, constraints = {"admin", "connector_manager"})
@List({@Redirect(code = "success", uri = "/admin/connector/"), @Redirect(code = "api-error", uri = "/admin/connector/"), @Redirect(code = "unsupported-connector-type", uri = "/admin/connector/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.connector = ((ConnectorResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveConnector(this.connectorId))).connector;
    return "input";
  }
  
  public String post() {
    if (this.connector.getType() == ConnectorType.Generic)
      ((GenericConnectorConfiguration)this.connector).headers.putAll(ActionTools.keyValueCollectionsToMap(this.headerNames, this.headerValues)); 
    BaseConnectorConfiguration baseConnectorConfiguration1 = ((ConnectorResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveConnector(this.connectorId))).connector;
    this.connector.data.clear();
    this.connector.data.putAll(baseConnectorConfiguration1.data);
    if (this.connector instanceof LDAPConnectorConfiguration && this.editPasswordOption == BaseFormAction.EditPasswordOption.useExisting)
      ((LDAPConnectorConfiguration)this.connector).systemAccountPassword = ((LDAPConnectorConfiguration)baseConnectorConfiguration1).systemAccountPassword; 
    BaseConnectorConfiguration baseConnectorConfiguration2 = ((ConnectorResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateConnector(this.connectorId, new ConnectorRequest(this.connector)))).connector;
    writeAuditLogForUpdate("Updated the connector with Id [" + String.valueOf(this.connectorId) + "] and name [" + baseConnectorConfiguration2.name + "]", baseConnectorConfiguration1, baseConnectorConfiguration2);
    return "success";
  }
}
