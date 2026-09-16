package io.fusionauth.api.service.email;

import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.email.EmailTemplate;
import org.primeframework.email.domain.EmailAddress;
import org.primeframework.email.domain.SendResult;
import org.primeframework.email.service.MessagingExceptionHandler;

public class FusionAuthMessagingExceptionHandler implements MessagingExceptionHandler {
  public void handle(MessagingExceptionHandler.PrimeMessagingException paramPrimeMessagingException) {
    StringBuilder stringBuilder = new StringBuilder("Async Email Send exception occurred.\n");
    SendResult sendResult = paramPrimeMessagingException.sendResult;
    Object object = sendResult.template;
    if (object instanceof EmailTemplate) {
      EmailTemplate emailTemplate = (EmailTemplate)object;
      stringBuilder.append("\nTemplate Id: ").append(emailTemplate.id);
      stringBuilder.append("\nTemplate Name: ").append(emailTemplate.name);
      stringBuilder.append("\nTenant Id: ").append(((Tenant)paramPrimeMessagingException.contextId).id);
      if (sendResult.email != null)
        for (EmailAddress emailAddress : sendResult.email.to) {
          stringBuilder.append("\nAddressed to: ");
          if (emailAddress.display != null)
            stringBuilder.append(emailAddress.display).append(" <"); 
          stringBuilder.append(emailAddress.address);
          if (emailAddress.display != null)
            stringBuilder.append(">"); 
        }  
    } 
    Throwable throwable = paramPrimeMessagingException.getCause();
    if (throwable != null)
      stringBuilder.append("\n\nCause:\n")
        .append(throwable.getClass().getCanonicalName())
        .append(" : ")
        .append("Message: ").append(throwable.getMessage()); 
    EventLogHelper.create(new EventLog(EventLogType.Error, stringBuilder.toString()));
  }
}
