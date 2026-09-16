package io.fusionauth.app.action.admin.connector;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.ConnectorResponse;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "connector_manager"})
public class IndexAction extends BaseAction {
  public List<BaseConnectorConfiguration> connectors;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.connectors = (List<BaseConnectorConfiguration>)Objects.requireNonNullElseGet(((ConnectorResponse)superDelegate().execute(FusionAuthClient::retrieveConnectors)).connectors, Collections::emptyList);
    return "input";
  }
}
