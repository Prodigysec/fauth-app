package io.fusionauth.app.action.ajax.theme;

import com.google.inject.Inject;
import com.inversoft.error.Error;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.system.ThemeService;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "theme_manager"})
public class ValidateLocalizationAction extends BaseAJAXAction {
  private final ThemeService themeService;
  
  public boolean isDefault;
  
  public String messages;
  
  public Map<String, String> missingValues = new LinkedHashMap<>();
  
  @Inject
  public ValidateLocalizationAction(FrontEndSupport paramFrontEndSupport, ThemeService paramThemeService) {
    super(paramFrontEndSupport);
    this.themeService = paramThemeService;
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    if (this.isDefault) {
      Errors errors = this.themeService.validateDefaultMessages(this.messages);
      if (errors.size() > 0) {
        Error error = ((List<Error>)errors.fieldErrors.get("theme.defaultMessages")).get(0);
        this.frontEndSupport.transfer((new Errors()).addFieldError("messages", "[invalid]messages", null, new Object[0]));
        try {
          Properties properties = new Properties();
          properties.load(new StringReader((String)error.values[1]));
          for (String str1 : properties.keySet()) {
            String str2 = str1;
            this.missingValues.put(str2, properties.getProperty(str2));
          } 
        } catch (IOException iOException) {
          throw new ErrorException(iOException, new Object[0]);
        } 
        return "input";
      } 
    } 
    return "success";
  }
}
