package io.fusionauth.app.freemarker;

import com.google.inject.Inject;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.List;
import org.primeframework.mvc.security.UserLoginSecurityContext;

public class HasOneRole implements TemplateMethodModelEx {
  private final UserLoginSecurityContext securityContext;
  
  @Inject
  public HasOneRole(UserLoginSecurityContext paramUserLoginSecurityContext) {
    this.securityContext = paramUserLoginSecurityContext;
  }
  
  public Object exec(List<Object> paramList) throws TemplateModelException {
    if (paramList.isEmpty())
      throw new TemplateModelException("You must supply at least one role"); 
    User user = (User)this.securityContext.getCurrentUser();
    UserRegistration userRegistration = user.getRegistrationForApplication(Application.FUSIONAUTH_APP_ID);
    if (paramList.size() == 1) {
      SimpleSequence simpleSequence = (SimpleSequence)paramList.get(0);
      if (simpleSequence instanceof SimpleSequence) {
        SimpleSequence simpleSequence1 = simpleSequence;
        for (byte b = 0; b < simpleSequence1.size(); b++) {
          if (userRegistration.roles.contains(simpleSequence1.get(b).toString()))
            return Boolean.valueOf(true); 
        } 
        return Boolean.valueOf(false);
      } 
    } 
    for (Object object : paramList) {
      if (userRegistration.roles.contains(object.toString()))
        return Boolean.valueOf(true); 
    } 
    return Boolean.valueOf(false);
  }
}
