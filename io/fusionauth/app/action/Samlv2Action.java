package io.fusionauth.app.action;

import com.google.inject.Inject;
import io.fusionauth.api.service.samlv2.SAMLv2ProviderService;
import io.fusionauth.app.action.samlv2.MetadataAction;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.annotation.Action;

@Action("{tenantId}")
public class Samlv2Action extends MetadataAction {
  @Inject
  public Samlv2Action(HTTPRequest paramHTTPRequest, SAMLv2ProviderService paramSAMLv2ProviderService) {
    super(paramHTTPRequest, paramSAMLv2ProviderService);
  }
}
