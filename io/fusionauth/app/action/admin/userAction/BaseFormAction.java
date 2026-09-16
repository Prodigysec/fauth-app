package io.fusionauth.app.action.admin.userAction;

import com.inversoft.util.StringTools;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.TransactionType;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionOption;
import io.fusionauth.domain.api.EmailTemplateResponse;
import io.fusionauth.domain.email.EmailTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

public class BaseFormAction extends BaseAction {
  @FTLVariable
  public static final TransactionType[] transactionTypes = TransactionType.values();
  
  public List<EmailTemplate> emailTemplates = new ArrayList<>();
  
  public UserAction userAction = new UserAction();
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @FormPrepareMethod
  public void loadDaEmailTemplates() {
    this.emailTemplates = (List<EmailTemplate>)Objects.requireNonNullElseGet(((EmailTemplateResponse)superDelegate().execute(FusionAuthClient::retrieveEmailTemplates)).emailTemplates, Collections::emptyList);
  }
  
  protected void cleanOptionsAndLocalizationData() {
    if (this.userAction.localizedNames != null)
      this.userAction.localizedNames.removeEmpty(); 
    this.userAction.options.removeIf(paramUserActionOption -> (paramUserActionOption == null || StringTools.isTrimmedEmpty(paramUserActionOption.name)));
    this.userAction.options.forEach(paramUserActionOption -> {
          if (paramUserActionOption.localizedNames != null)
            paramUserActionOption.localizedNames.removeEmpty(); 
        });
  }
}
