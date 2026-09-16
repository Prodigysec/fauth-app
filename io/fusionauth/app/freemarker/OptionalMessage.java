package io.fusionauth.app.freemarker;

import com.google.inject.Inject;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;
import org.primeframework.mvc.message.l10n.MessageProvider;

public class OptionalMessage implements TemplateMethodModelEx {
  private final MessageProvider messageProvider;
  
  @Inject
  public OptionalMessage(MessageProvider paramMessageProvider) {
    this.messageProvider = paramMessageProvider;
  }
  
  public Object exec(List<E> paramList) throws TemplateModelException {
    if (paramList.isEmpty())
      throw new TemplateModelException("Missing message key."); 
    String str1 = paramList.get(0).toString();
    String str2 = this.messageProvider.getOptionalMessage(str1, new Object[0]);
    return (str2 == null) ? "" : str2;
  }
}
