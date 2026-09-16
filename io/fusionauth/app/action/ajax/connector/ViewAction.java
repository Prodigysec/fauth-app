package io.fusionauth.app.action.ajax.connector;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.ConnectorResponse;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{connectorId}", requiresAuthentication = true, constraints = {"admin", "connector_manager"})
public class ViewAction extends BaseAJAXAction {
  public BaseConnectorConfiguration connector;
  
  public UUID connectorId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.connector = ((ConnectorResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveConnector(this.connectorId))).connector;
    return "render";
  }
}
