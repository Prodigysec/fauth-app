package io.fusionauth.app.guice;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionAuthFreeMarkerTemplateExceptionHandler implements TemplateExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(FusionAuthFreeMarkerTemplateExceptionHandler.class);
  
  private final FusionAuthConfiguration configuration;
  
  public FusionAuthFreeMarkerTemplateExceptionHandler(FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.configuration = paramFusionAuthConfiguration;
  }
  
  public void handleTemplateException(TemplateException paramTemplateException, Environment paramEnvironment, Writer paramWriter) throws TemplateException {
    PrintWriter printWriter1 = (paramWriter instanceof PrintWriter) ? (PrintWriter)paramWriter : new PrintWriter(paramWriter);
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter2 = new PrintWriter(stringWriter);
    paramTemplateException.printStackTrace(printWriter2);
    RuntimeMode runtimeMode = this.configuration.runtimeMode();
    Integer integer = (Integer)paramEnvironment.getCustomAttribute("handledTemplateExceptions");
    integer = Integer.valueOf((integer == null) ? 1 : (integer.intValue() + 1));
    paramEnvironment.setCustomAttribute("handledTemplateExceptions", integer);
    String str = "\"><div class=\"code\" style=\"padding: 5px;\">\n  A FreeMarker exception occurred.";
    if (runtimeMode != RuntimeMode.FusionAuth_Development)
      str = str + " See the FusionAuth Event Log for additional details.\n"; 
    if (runtimeMode == RuntimeMode.Development || runtimeMode == RuntimeMode.FusionAuth_Development) {
      String str1 = paramTemplateException.getTemplateSourceName();
      String str2 = null;
      if (str1.startsWith("##")) {
        str2 = str1.substring(2, str1.indexOf("##", 2));
        str1 = str1.substring(str1.indexOf("##templates") + 11);
      } 
      str = str + str;
      if (runtimeMode == RuntimeMode.FusionAuth_Development)
        str = str + str; 
    } 
    str = str + "</div>";
    if (runtimeMode != RuntimeMode.Production || integer.intValue() == 1)
      printWriter1.write(str); 
    if (runtimeMode == RuntimeMode.FusionAuth_Development) {
      logger.error("FreeMarker template exception: template=[{}] line=[{}] expression=[{}]", new Object[] { paramTemplateException
            .getTemplateSourceName(), paramTemplateException.getLineNumber(), paramTemplateException.getBlamedExpressionString(), paramTemplateException });
      throw paramTemplateException;
    } 
    EventLogHelper.create(new EventLog(EventLogType.Error, "A FreeMarker exception occurred.", (Throwable)paramTemplateException));
  }
  
  private String defaultIfNull(String paramString) {
    return (paramString != null) ? paramString : "–";
  }
  
  private String getStackTrace(Exception paramException) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    paramException.printStackTrace(printWriter);
    return stringWriter.toString();
  }
}
