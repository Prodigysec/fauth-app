package io.fusionauth.app.action.ajax.oauth2;

import com.google.inject.Inject;
import com.inversoft.util.SecurityTools;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import java.util.HashMap;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager", "entity_manager"})
public class RegenerateClientSecretAction extends BaseAJAXAction {
  @JSONResponse
  public Map<String, String> response = new HashMap<>();
  
  @FTLVariable
  public String warnKey = "warn";
  
  @Inject
  public RegenerateClientSecretAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    this.response.put("clientSecret", SecurityTools.secureRandom());
    return "render-json";
  }
}
