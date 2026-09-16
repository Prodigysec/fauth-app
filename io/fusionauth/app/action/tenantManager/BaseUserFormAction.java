package io.fusionauth.app.action.tenantManager;

import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.util.LocaleTools;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.user.CustomFormFrontendService;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.domain.form.ManagedFields;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Stream;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

public class BaseUserFormAction extends BaseTenantManagerAction {
  @FTLVariable
  public static final SortedSet<String> timezones = new TreeSet<>(ZoneId.getAvailableZoneIds());
  
  protected final CustomFormFrontendService customFormFrontendService;
  
  @FTLVariable
  public CustomFormFrontendService.ConfirmUser confirm = new CustomFormFrontendService.ConfirmUser();
  
  @FTLVariable
  public Map<Integer, List<FormField>> fields = new HashMap<>();
  
  @FTLVariable
  public List<FormField> formFields = Collections.emptyList();
  
  @FTLVariable
  public List<Locale> locales = LocaleTools.availableLocales();
  
  @FTLVariable
  public Set<String> managedFields = ManagedFields.Values
    .keySet();
  
  @FTLVariable
  public User user = new User();
  
  public BaseUserFormAction(UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, CSRFProvider paramCSRFProvider, UUID paramUUID, CustomFormFrontendService paramCustomFormFrontendService) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
    this.customFormFrontendService = paramCustomFormFrontendService;
  }
  
  @FormPrepareMethod
  public void formPrepare() {
    if (this.tenant != null && this.tenant.formConfiguration != null) {
      UUID uUID = this.tenant.formConfiguration.adminUserFormId;
      if (uUID != null)
        this.fields = this.customFormFrontendService.retrieveFieldsByFormId(uUID); 
    } 
  }
  
  @PostValidationMethod
  public void postValidation() {
    this

      
      .formFields = getFormFields().stream().filter(paramFormField -> !paramFormField.key.equals("user.password")).toList();
  }
  
  protected List<FormField> getFormFields() {
    UUID uUID = this.tenant.formConfiguration.adminUserFormId;
    if (uUID != null) {
      ClientResponse<FormResponse, Void> clientResponse = this.superClient.retrieveForm(uUID);
      if (clientResponse.wasSuccessful()) {
        Form form = ((FormResponse)clientResponse.successResponse).form;
        return form.steps
          .stream()
          .flatMap(paramFormStep -> paramFormStep.fields.stream().map(()).filter(ClientResponse::wasSuccessful).map(()))






          
          .peek(paramFormField -> {
              if (paramFormField.key.equals("user.timezone")) {
                paramFormField.options.addAll(timezones);
              } else if (paramFormField.key.equals("user.preferredLanguages") || paramFormField.key.equals("registration.preferredLanguages")) {
                paramFormField.options.addAll(this.locales.stream().map(Locale::toLanguageTag).toList());
              } 
            }).toList();
      } 
    } 
    return List.of();
  }
}
