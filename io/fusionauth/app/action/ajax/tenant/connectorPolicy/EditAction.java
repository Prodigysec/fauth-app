package io.fusionauth.app.action.ajax.tenant.connectorPolicy;

import com.google.inject.Inject;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "tenant_manager"})
public class EditAction extends AddAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
}
