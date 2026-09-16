package io.fusionauth.api.service.email;

import com.google.inject.Inject;
import com.inversoft.util.StringTools;
import freemarker.core.OutputFormat;
import freemarker.core.ParseException;
import freemarker.core.ParserConfiguration;
import freemarker.core.TemplateConfiguration;
import freemarker.core.UndefinedOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.fusionauth.api.domain.EmailTemplateMapper;
import io.fusionauth.api.l10n.Localizer;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.email.EmailTemplate;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.primeframework.email.domain.BaseResult;
import org.primeframework.email.domain.ParsedEmailAddress;
import org.primeframework.email.domain.ParsedEmailTemplates;
import org.primeframework.email.guice.Email;
import org.primeframework.email.service.BaseEmailTemplateLoader;

public class DatabaseEmailTemplateLoader extends BaseEmailTemplateLoader {
  private static final String LegacyEscapingErrorDescription = "(legacy escaping) is not allowed when auto-escaping is on with a markup output format (HTML)";
  
  private final EmailTemplateMapper emailTemplateMapper;
  
  private final TemplateConfiguration fallbackTemplateConfiguration;
  
  @Inject
  public DatabaseEmailTemplateLoader(EmailTemplateMapper paramEmailTemplateMapper, @Email Configuration paramConfiguration) {
    super(paramConfiguration);
    this.emailTemplateMapper = paramEmailTemplateMapper;
    TemplateConfiguration templateConfiguration = new TemplateConfiguration();
    templateConfiguration.setParentConfiguration(paramConfiguration);
    templateConfiguration.setOutputFormat((OutputFormat)UndefinedOutputFormat.INSTANCE);
    this.fallbackTemplateConfiguration = templateConfiguration;
  }
  
  public ParsedEmailTemplates load(Object paramObject1, Object paramObject2, List<Locale> paramList, BaseResult paramBaseResult) {
    EmailTemplate emailTemplate = this.emailTemplateMapper.retrieveById((UUID)paramObject2);
    if (emailTemplate == null) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Unable to load email template by Id [" + String.valueOf(paramObject2) + "].\nReview all usages of the referenced Email Template Id if non-null. If the value is null, this is almost certainly a bug in FusionAuth. Please open a GitHub issue or contact open a ticket with support."));
      throw new EmailTemplateNotFoundException((UUID)paramObject2);
    } 
    paramBaseResult.template = emailTemplate;
    paramBaseResult.templateId = paramObject2;
    String str1 = Localizer.localize(paramList, emailTemplate.localizedSubjects, emailTemplate.defaultSubject);
    ParsedEmailTemplates parsedEmailTemplates = new ParsedEmailTemplates();
    if (str1 != null)
      parsedEmailTemplates.subject = parseTemplate(str1, "subject", paramBaseResult); 
    String str2 = StringTools.defaultIfNull(emailTemplate.fromEmail, ((Tenant)paramObject1).emailConfiguration.defaultFromEmail);
    parsedEmailTemplates.from = new ParsedEmailAddress(str2);
    String str3 = StringTools.defaultIfNull(Localizer.localize(paramList, emailTemplate.localizedFromNames, emailTemplate.defaultFromName), ((Tenant)paramObject1).emailConfiguration.defaultFromName);
    if (str3 != null)
      parsedEmailTemplates.from.display = parseTemplate(str3, "from", paramBaseResult); 
    String str4 = Localizer.localize(paramList, emailTemplate.localizedHtmlTemplates, emailTemplate.defaultHtmlTemplate);
    if (str4 != null)
      parsedEmailTemplates.html = parseTemplateHandleHTMLEscapingInTemplate(str4, "html", paramBaseResult); 
    String str5 = Localizer.localize(paramList, emailTemplate.localizedTextTemplates, emailTemplate.defaultTextTemplate);
    if (str5 != null)
      parsedEmailTemplates.text = parseTemplateHandleHTMLEscapingInTemplate(str5, "text", paramBaseResult); 
    return parsedEmailTemplates;
  }
  
  protected Template parseTemplateHandleHTMLEscapingInTemplate(String paramString1, String paramString2, BaseResult paramBaseResult) {
    if (paramString1 == null)
      return null; 
    try {
      return new Template(null, paramString1, this.freeMarkerConfiguration);
    } catch (ParseException parseException) {
      try {
        String str = parseException.getEditorMessage();
        if (str != null && str.contains("(legacy escaping) is not allowed when auto-escaping is on with a markup output format (HTML)"))
          return new Template(null, null, new StringReader(paramString1), this.freeMarkerConfiguration, (ParserConfiguration)this.fallbackTemplateConfiguration, null); 
        paramBaseResult.parseErrors.put(paramString2, parseException);
      } catch (ParseException parseException1) {
        paramBaseResult.parseErrors.put(paramString2, parseException1);
      } catch (IOException iOException) {}
    } catch (IOException iOException) {}
    return null;
  }
}
