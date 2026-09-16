package io.fusionauth.app.action.ajax.theme;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.ThemeService;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, constraints = {"admin", "theme_manager"})
public class CopyDefaultTemplateAction extends BaseAJAXAction {
  @JSONResponse
  public CopiedTemplateResponse response;
  
  public String templateName;
  
  @Inject
  public CopyDefaultTemplateAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    String str = ThemeService.templateFromFilesystem(this.frontEndSupport.context.resolve("/"), this.templateName);
    this.response = new CopiedTemplateResponse(str);
    return "render-json";
  }
  
  public static class CopiedTemplateResponse {
    public String template;
    
    public CopiedTemplateResponse(String param1String) {
      this.template = param1String;
    }
  }
}
