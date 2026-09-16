package io.fusionauth.api.service.email;

import com.google.inject.Inject;
import io.fusionauth.domain.EmailHeader;
import io.fusionauth.domain.Tenant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.primeframework.email.domain.BaseResult;
import org.primeframework.email.domain.EmailHeader;
import org.primeframework.email.domain.RawEmailTemplates;
import org.primeframework.email.domain.ValidateResult;
import org.primeframework.email.service.DefaultEmailService;
import org.primeframework.email.service.EmailRenderer;
import org.primeframework.email.service.EmailTemplateLoader;
import org.primeframework.email.service.EmailTransportService;
import org.primeframework.email.service.SendEmailBuilder;

public class FusionAuthEmailService extends DefaultEmailService {
  @Inject
  public FusionAuthEmailService(EmailRenderer paramEmailRenderer, EmailTemplateLoader paramEmailTemplateLoader, EmailTransportService paramEmailTransportService) {
    super(paramEmailRenderer, paramEmailTemplateLoader, paramEmailTransportService);
  }
  
  public SendEmailBuilder send(Object paramObject1, Object paramObject2, List<Locale> paramList) {
    Tenant tenant;
    if (paramObject1 instanceof Tenant) {
      tenant = (Tenant)paramObject1;
    } else {
      throw new IllegalArgumentException("Expecting a tenant for the contextId but found [" + String.valueOf(paramObject1.getClass()) + "]");
    } 
    return (SendEmailBuilder)super.send(paramObject1, paramObject2, paramList)
      .withHeaders((List)tenant.emailConfiguration.additionalHeaders.stream()
        .map(paramEmailHeader -> new EmailHeader(paramEmailHeader.name, paramEmailHeader.value))
        .collect(Collectors.toList()));
  }
  
  public ValidateResult validate(Object paramObject, RawEmailTemplates paramRawEmailTemplates, Map<String, Object> paramMap) {
    ValidateResult validateResult = new ValidateResult();
    this.emailTemplateLoader.parse(paramRawEmailTemplates, (BaseResult)validateResult);
    return validateResult;
  }
}
