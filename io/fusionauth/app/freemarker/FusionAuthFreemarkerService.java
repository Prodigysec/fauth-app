package io.fusionauth.app.freemarker;

import com.google.inject.Inject;
import freemarker.template.Configuration;
import io.fusionauth.app.primeframework.FusionAuthTemplateLoader;
import io.fusionauth.domain.Theme;
import java.io.Writer;
import org.primeframework.mvc.freemarker.DefaultFreeMarkerService;
import org.primeframework.mvc.freemarker.MissingTemplateException;
import org.primeframework.mvc.locale.LocaleProvider;

public class FusionAuthFreemarkerService extends DefaultFreeMarkerService {
  @Inject
  public FusionAuthFreemarkerService(Configuration paramConfiguration, LocaleProvider paramLocaleProvider) {
    super(paramConfiguration, paramLocaleProvider);
  }
  
  public void render(Writer paramWriter, String paramString, Object paramObject) {
    try {
      super.render(paramWriter, paramString, paramObject);
    } catch (MissingTemplateException missingTemplateException) {
      FusionAuthTemplateLoader.ThemedTemplate themedTemplate = FusionAuthTemplateLoader.ThemedPageEncoder.decode(paramString);
      if (themedTemplate.themeId != null && themedTemplate.themeId != Theme.FUSIONAUTH_THEME_ID) {
        String str = FusionAuthTemplateLoader.ThemedPageEncoder.encode(themedTemplate.name, Theme.FUSIONAUTH_THEME_ID);
        super.render(paramWriter, str, paramObject);
      } else {
        throw new MissingTemplateException("Template not found in theme or default FusionAuth theme: " + themedTemplate.name, missingTemplateException);
      } 
    } 
  }
}
