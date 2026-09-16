package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.connector.ConnectorConfigurationService;
import io.fusionauth.api.service.connector.ConnectorValidator;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ConnectorRequest;
import io.fusionauth.domain.api.ConnectorResponse;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorType;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{connectorId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class ConnectorAction extends BaseAPIAction implements Patchable {
  private final ConnectorConfigurationService connectorConfigurationService;
  
  private final Map<ConnectorType, ConnectorValidator> validators;
  
  @PreParameter
  public UUID connectorId;
  
  @JSONPatch
  @JSONRequest
  public ConnectorRequest request = new ConnectorRequest();
  
  @JSONResponse
  public ConnectorResponse response;
  
  private ConnectorConfigurationService.ValidationResult result;
  
  @Inject
  public ConnectorAction(FrontEndSupport paramFrontEndSupport, ConnectorConfigurationService paramConnectorConfigurationService, Map<ConnectorType, ConnectorValidator> paramMap) {
    super(paramFrontEndSupport);
    this.connectorConfigurationService = paramConnectorConfigurationService;
    this.validators = paramMap;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.connectorConfigurationService.delete(this.connectorId);
    return "success";
  }
  
  public String get() {
    if (this.connectorId == null) {
      this.response = new ConnectorResponse(this.connectorConfigurationService.retrieveAll());
    } else {
      BaseConnectorConfiguration baseConnectorConfiguration = this.connectorConfigurationService.retrieveById(this.connectorId);
      if (baseConnectorConfiguration == null)
        return "missing"; 
      this.response = new ConnectorResponse(baseConnectorConfiguration);
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.connectorId != null)
      this.request.connector = this.connectorConfigurationService.retrieveById(this.connectorId); 
  }
  
  public String post() {
    this.connectorConfigurationService.create(this.request.connector);
    this.response = new ConnectorResponse(this.request.connector);
    return "render";
  }
  
  public String put() throws Exception {
    if (this.result.existing == null)
      return "missing"; 
    this.connectorConfigurationService.update(this.result.existing, this.result.connector);
    this.response = new ConnectorResponse(this.result.connector);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.request.connector == null) {
      this.frontEndSupport.addFieldError("connector", "[missing]connector", new Object[0]);
      return;
    } 
    if (this.connectorId != null)
      this.request.connector.id = this.connectorId; 
    this.result = this.connectorConfigurationService.validate(this.request.connector, this.frontEndSupport.isPOST());
    ConnectorValidator connectorValidator = this.validators.get(this.request.connector.getType());
    if (connectorValidator != null)
      this.result.errors.add(connectorValidator.validate(this.request.connector, this.frontEndSupport.isPOST())); 
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.connectorId == null) {
      this.frontEndSupport.addFieldError("connectorId", "[missing]connectorId", new Object[0]);
      return;
    } 
    if (this.connectorId.equals(BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID)) {
      this.frontEndSupport.addFieldError("connectorId", "[fusionAuth]connectorId", new Object[0]);
      return;
    } 
    this.result = this.connectorConfigurationService.validateDelete(this.connectorId);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
