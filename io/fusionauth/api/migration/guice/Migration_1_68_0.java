package io.fusionauth.api.migration.guice;

import com.google.inject.Inject;
import com.inversoft.migration.Migration;
import io.fusionauth.api.service.message.MessageTemplateService;
import io.fusionauth.api.service.system.SystemDefaultsSingleton;
import io.fusionauth.domain.message.MessageTemplate;
import org.savantbuild.domain.Version;

public class Migration_1_68_0 implements Migration {
  private final MessageTemplateService messageTemplateService;
  
  private final SystemDefaultsSingleton systemDefaults;
  
  @Inject
  public Migration_1_68_0(MessageTemplateService paramMessageTemplateService, SystemDefaultsSingleton paramSystemDefaultsSingleton) {
    this.messageTemplateService = paramMessageTemplateService;
    this.systemDefaults = paramSystemDefaultsSingleton;
  }
  
  public void cleanup() throws Exception {}
  
  public void runOnce() {
    this.systemDefaults.set(new Version("1.68.0"));
    MessageTemplate messageTemplate1 = this.messageTemplateService.retrieveByName("Default Two Factor Request");
    if (messageTemplate1 != null && (this.messageTemplateService.validateDelete(messageTemplate1.id)).errors.empty())
      this.messageTemplateService.delete(messageTemplate1); 
    MessageTemplate messageTemplate2 = this.messageTemplateService.retrieveByName("Default Voice Two Factor Request");
    if (messageTemplate2 != null && (this.messageTemplateService.validateDelete(messageTemplate2.id)).errors.empty())
      this.messageTemplateService.delete(messageTemplate2); 
  }
}
