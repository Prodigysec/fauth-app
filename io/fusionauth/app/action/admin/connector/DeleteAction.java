package io.fusionauth.app.action.admin.connector;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.ConnectorResponse;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{connectorId}", requiresAuthentication = true, constraints = {"admin", "connector_deleter"})
@List({@Redirect(code = "success", uri = "/admin/connector/"), @Redirect(code = "missing", uri = "/admin/connector/")})
public class DeleteAction extends BaseAction {
  public String confirm;
  
  public BaseConnectorConfiguration connector;
  
  public UUID connectorId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.customPreTransferErrorConsumer = (paramErrors -> paramFrontEndSupport.moveFieldErrorToGeneral(paramErrors, "connectorId", "[inUseByTenant]connectorId"));
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteConnector(this.connectorId));
    writeAuditLog("Deleted the connector with Id [" + String.valueOf(this.connectorId) + "] and name [" + this.connector.name + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void retrieveConnector() {
    this.connector = ((ConnectorResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveConnector(this.connectorId))).connector;
  }
  
  @ValidationMethod
  public void validate() {
    if (this.confirm == null) {
      this.frontEndSupport.addFieldError("confirm", "[missing]confirm", new Object[0]);
    } else if (!this.confirm.equals("DELETE")) {
      this.frontEndSupport.addFieldError("confirm", "[invalid]confirm", new Object[0]);
    } 
  }
}
