package io.fusionauth.api.service.email;

import com.google.inject.Inject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.fusionauth.api.util.EmailTools;
import io.fusionauth.domain.EmailHeader;
import io.fusionauth.domain.Tenant;
import java.io.IOException;
import java.util.HashMap;
import org.primeframework.email.domain.BaseResult;
import org.primeframework.email.domain.Email;
import org.primeframework.email.domain.EmailAddress;
import org.primeframework.email.domain.EmailHeader;
import org.primeframework.email.domain.ParsedEmailTemplates;
import org.primeframework.email.domain.SendResult;
import org.primeframework.email.service.EmailRenderer;
import org.primeframework.email.service.EmailTransportService;
import org.primeframework.email.service.MessagingExceptionHandler;

public class DefaultSMTPTestService implements SMTPTestService {
  private final Configuration configuration;
  
  private final EmailRenderer emailRenderer;
  
  private final EmailTransportService emailTransportService;
  
  @Inject
  public DefaultSMTPTestService(Configuration paramConfiguration, EmailRenderer paramEmailRenderer, EmailTransportService paramEmailTransportService) {
    this.configuration = paramConfiguration;
    this.emailRenderer = paramEmailRenderer;
    this.emailTransportService = paramEmailTransportService;
  }
  
  public SMTPTestService.SMTPTestResult test(Tenant paramTenant, String paramString) {
    Email email = new Email();
    email.to.add(new EmailAddress(paramString));
    email.subject = "FusionAuth SMTP Test";
    email.from = new EmailAddress((paramTenant.emailConfiguration.defaultFromEmail == null) ? "no-reply@fusionauth.io" : paramTenant.emailConfiguration.defaultFromEmail);
    email.replyTo = email.from;
    paramTenant.emailConfiguration.additionalHeaders.forEach(paramEmailHeader -> paramEmail.additionalHeaders.add(new EmailHeader(paramEmailHeader.name, paramEmailHeader.value)));
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("tenant", paramTenant);
    SMTPTestService.SMTPTestResult sMTPTestResult = new SMTPTestService.SMTPTestResult();
    ParsedEmailTemplates parsedEmailTemplates = new ParsedEmailTemplates();
    try {
      parsedEmailTemplates.html = new Template(null, EmailTools.loadTemplate(getClass(), "/emails/internal/smtp-test.html.ftl"), this.configuration);
      parsedEmailTemplates.text = new Template(null, EmailTools.loadTemplate(getClass(), "/emails/internal/smtp-test.txt.ftl"), this.configuration);
    } catch (IOException iOException) {
      sMTPTestResult.exception = iOException;
      sMTPTestResult.message = iOException.getMessage();
      return sMTPTestResult;
    } 
    SendResult sendResult = new SendResult(email);
    this.emailRenderer.render(parsedEmailTemplates, email, hashMap, (BaseResult)sendResult);
    this.emailTransportService.sendEmail(paramTenant, email, sendResult, paramPrimeMessagingException -> paramSMTPTestResult.exception = (Exception)paramPrimeMessagingException);
    sMTPTestResult.message = sendResult.transportError;
    sMTPTestResult.success = (sMTPTestResult.exception == null);
    if (sMTPTestResult.exception != null) {
      if (sMTPTestResult.message != null && sMTPTestResult.message.length() > 0)
        sMTPTestResult.message += "\n\n"; 
      sMTPTestResult.message += sMTPTestResult.message;
      Throwable throwable = sMTPTestResult.exception.getCause();
      if (throwable != null) {
        sMTPTestResult.message = sMTPTestResult.message + "\n" + sMTPTestResult.message;
        sMTPTestResult.message = sMTPTestResult.message.replaceAll("[\n\r]$", "");
        Throwable throwable1 = throwable.getCause();
        if (throwable1 != null) {
          sMTPTestResult.message = sMTPTestResult.message + "\nCause: " + sMTPTestResult.message + ": " + throwable1.getClass().getSimpleName();
          sMTPTestResult.message = sMTPTestResult.message.replaceAll("[\n\r]$", "");
        } 
      } 
    } 
    return sMTPTestResult;
  }
}
