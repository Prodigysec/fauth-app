package io.fusionauth.app.action.support;

import com.google.inject.Inject;
import com.inversoft.support.service.SupportService;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
@List({@Redirect(uri = "/"), @Redirect(code = "done", uri = "/support/forgot-password")})
public class ForgotPasswordAction extends BaseAction {
  private final SupportService supportService;
  
  private final TenantCache tenantCache;
  
  private final UserReaderService userReader;
  
  private final UserService userService;
  
  public String authorizationCode;
  
  public String five;
  
  public String four;
  
  public String one;
  
  public String three;
  
  public String two;
  
  private Tenant codeTenant;
  
  private User codeUser;
  
  @Inject
  public ForgotPasswordAction(FrontEndSupport paramFrontEndSupport, SupportService paramSupportService, TenantCache paramTenantCache, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramFrontEndSupport);
    this.supportService = paramSupportService;
    this.tenantCache = paramTenantCache;
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
  }
  
  public String get() {
    if (!this.frontEndSupport.configuration.supportLoginEnabled())
      return "success"; 
    return "input";
  }
  
  public String post() {
    if (!this.frontEndSupport.configuration.supportLoginEnabled())
      return "success"; 
    if (this.one.endsWith("@fusionauth.io") && this.codeTenant != null && this.codeUser != null) {
      UserService.ForgotPasswordResult forgotPasswordResult = this.userService.forgotPassword(this.codeTenant, null, this.codeUser, this.codeUser.resolvePrimaryIdentity(IdentityType.email), this.two, null, false, null, this.frontEndSupport.buildEventInfo(null));
      String str = this.frontEndSupport.getFusionAuthBaseURL() + "/password/change/" + this.frontEndSupport.getFusionAuthBaseURL();
      this.supportService.sendEmail(this.two, "FusionAuth Support Reset Password Request", str, "support@fusionauth.io", "FusionAuth Support", this.four, this.five);
    } 
    this.frontEndSupport.addGeneralInfo("forgotPasswordEmailSent", new Object[0]);
    return "done";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.one == null || this.one.length() == 0)
      this.frontEndSupport.addFieldError("one", "[blank]one", new Object[0]); 
    if (this.two == null || this.two.length() == 0)
      this.frontEndSupport.addFieldError("two", "[blank]two", new Object[0]); 
    this.tenantId = (this.three != null) ? StringTools.parseUUID(this.three) : null;
    this.codeTenant = (this.tenantId != null) ? (Tenant)this.tenantCache.get(this.tenantId) : (Tenant)this.tenantCache.get(this.tenantCache.getDefaultTenantId());
    if (this.two != null && this.codeTenant != null)
      this.codeUser = this.userReader.retrieveByLoginId(this.codeTenant.id, this.two, List.of(IdentityType.email)); 
    if (this.four == null || this.four.length() == 0)
      this.frontEndSupport.addFieldError("four", "[blank]four", new Object[0]); 
    if (this.five == null || this.five.length() == 0)
      this.frontEndSupport.addFieldError("five", "[blank]five", new Object[0]); 
  }
}
