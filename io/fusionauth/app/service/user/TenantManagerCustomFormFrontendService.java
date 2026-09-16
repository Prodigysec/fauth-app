package io.fusionauth.app.service.user;

import com.google.inject.Inject;
import io.fusionauth.api.service.consent.ConsentService;
import io.fusionauth.api.service.form.FormService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.form.FormField;
import java.util.ArrayList;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class TenantManagerCustomFormFrontendService extends DefaultCustomFormFrontendService {
  @Inject
  public TenantManagerCustomFormFrontendService(ApplicationReaderService paramApplicationReaderService, FusionAuthClient paramFusionAuthClient, ConsentService paramConsentService, ExpressionEvaluator paramExpressionEvaluator, FormService paramFormService) {
    super(paramApplicationReaderService, paramFusionAuthClient, paramConsentService, paramExpressionEvaluator, paramFormService);
  }
  
  public CustomFormFrontendService.ValidationResult<User> normalizeAndValidate(Tenant paramTenant, User paramUser1, User paramUser2, User paramUser3, CustomFormFrontendService.EditPasswordOption paramEditPasswordOption) {
    ArrayList<FormField> arrayList = new ArrayList<>(this.formService.retrieveFieldsByFormId(paramTenant.formConfiguration.adminUserFormId));
    User user1 = retrieveUser(paramTenant, paramUser1.id);
    User user2 = merge(this.expressionEvaluator, paramUser1, user1, arrayList);
    handleEditPasswordOptions(paramEditPasswordOption, true, arrayList, user2);
    return normalizeAndValidateFormFields(user2, paramUser2, arrayList);
  }
}
