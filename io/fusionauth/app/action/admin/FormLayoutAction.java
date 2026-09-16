package io.fusionauth.app.action.admin;

import com.google.inject.Inject;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.service.system.SetupService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.ReportUtil;
import io.fusionauth.domain.DisplayableRawLogin;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action
public class FormLayoutAction extends BaseAction {
  @FTLVariable
  public final UUID fusionAuthTenantId;
  
  public IndexAction.IndexReport dailyActiveUserReport;
  
  @FTLVariable
  public SetupService.FirstTimeSetup firstTimeSetup;
  
  public ReportUtil.ReportData loginData;
  
  public IndexAction.IndexReport loginReport;
  
  public List<DisplayableRawLogin> logins;
  
  public IndexAction.IndexReport registrationReport;
  
  @FTLVariable
  public String text;
  
  @FTLVariable
  public String textLeftAddOn;
  
  @FTLVariable
  public String textLeftAddOnRaw;
  
  @FTLVariable
  public String textLeftAddOnText;
  
  @FTLVariable
  public String textRequired;
  
  @FTLVariable
  public String textRightAddOn;
  
  @FTLVariable
  public String textRightAddOnRaw;
  
  public IndexAction.IndexReport totalsReport;
  
  @Inject
  public FormLayoutAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    this.fusionAuthTenantId = paramFrontEndSupport.fusionAuthTenantId;
    paramFrontEndSupport.context.setAttribute("ProxyTestNonce", SecurityTools.secureRandom());
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    if (this.textRequired == null || this.textRequired.isBlank())
      this.frontEndSupport.addFieldError("textRequired", "[blank]textRequired", new Object[0]); 
    return "input";
  }
}
