package io.fusionauth.app.freemarker;

import com.google.inject.Inject;
import freemarker.ext.beans.CollectionModel;
import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.primeframework.mvc.locale.LocaleProvider;

public class DisplayLocaleNames implements TemplateMethodModelEx {
  private static final String ERROR_MESSAGE = "You must pass an object like this:\n\n  display_locale_names(object)";
  
  private final LocaleProvider localeProvider;
  
  @Inject
  public DisplayLocaleNames(LocaleProvider paramLocaleProvider) {
    this.localeProvider = paramLocaleProvider;
  }
  
  public Object exec(List<Object> paramList) throws TemplateModelException {
    List list;
    if (paramList.size() != 1)
      throw new TemplateModelException("You must pass an object like this:\n\n  display_locale_names(object)"); 
    Object object = paramList.get(0);
    if (object instanceof SimpleSequence) {
      SimpleSequence simpleSequence = (SimpleSequence)paramList.get(0);
      list = new ArrayList(simpleSequence.size());
      for (byte b = 0; b < simpleSequence.size(); b++) {
        SimpleScalar simpleScalar = (SimpleScalar)simpleSequence.get(b);
        list.add(new Locale(simpleScalar.getAsString()));
      } 
    } else {
      CollectionModel collectionModel = (CollectionModel)paramList.get(0);
      list = (List)collectionModel.getWrappedObject();
    } 
    Locale locale = (Locale)this.localeProvider.get();
    return list.stream().map(paramLocale2 -> paramLocale2.getDisplayName(paramLocale1)).collect(Collectors.joining(", "));
  }
}
