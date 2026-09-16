package io.fusionauth.app.action.samlv2;

import com.google.inject.Inject;
import io.fusionauth.api.service.samlv2.SAMLv2ProviderService;
import io.fusionauth.app.action.BaseFasterAction;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.samlv2.domain.SAMLException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.XMLStream;

@Action("{tenantId}")
@XMLStream(property = "response")
@Forward(code = "input", page = "/errors/404.ftl", status = 404)
public class MetadataAction extends BaseFasterAction {
  private final HTTPRequest request;
  
  private final SAMLv2ProviderService samlv2ProviderService;
  
  public String response;
  
  @Inject
  public MetadataAction(HTTPRequest paramHTTPRequest, SAMLv2ProviderService paramSAMLv2ProviderService) {
    this.request = paramHTTPRequest;
    this.samlv2ProviderService = paramSAMLv2ProviderService;
  }
  
  public String get() throws SAMLException {
    String str = this.request.getBaseURL();
    this.response = this.samlv2ProviderService.buildIdPMetaDataResponse(this.tenantId, str);
    return "success";
  }
}
