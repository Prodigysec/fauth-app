package io.fusionauth.app.action.samlv2;

import com.google.inject.Inject;
import io.fusionauth.api.service.samlv2.SAMLv2ProviderService;
import io.fusionauth.app.action.samlv2.sp.MetadataAction;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.annotation.Action;

@Action("{identityProviderId}")
public class SpAction extends MetadataAction {
  @Inject
  public SpAction(HTTPRequest paramHTTPRequest, SAMLv2ProviderService paramSAMLv2ProviderService) {
    super(paramHTTPRequest, paramSAMLv2ProviderService);
  }
}
