package io.fusionauth.app.action.admin;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.APIKeyReaderService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.Application;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;

@Action
public class SetupWizardDoneAction extends BaseAction {
  private final APIKeyReaderService apiKeyReader;
  
  private final ApplicationReaderService applicationReader;
  
  public String apiKey;
  
  public Application application;
  
  @Inject
  public SetupWizardDoneAction(FrontEndSupport paramFrontEndSupport, ApplicationReaderService paramApplicationReaderService, APIKeyReaderService paramAPIKeyReaderService) {
    super(paramFrontEndSupport);
    this.applicationReader = paramApplicationReaderService;
    this.apiKeyReader = paramAPIKeyReaderService;
  }
  
  public String get() {
    this.application = this.applicationReader.retrieveById(null, Application.FUSIONAUTH_APP_ID);
    List<APIKey> list = this.apiKeyReader.retrieveAll(null);
    this.apiKey = list.isEmpty() ? null : ((APIKey)list.get(0)).key;
    return "input";
  }
}
