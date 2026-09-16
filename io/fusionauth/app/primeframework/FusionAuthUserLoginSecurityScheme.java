package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.inversoft.util.StringTools;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.app.primeframework.exceptions.InvalidRefererException;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import java.net.URI;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.UnauthorizedException;
import org.primeframework.mvc.security.UserLoginConstraintsValidator;
import org.primeframework.mvc.security.UserLoginSecurityScheme;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionAuthUserLoginSecurityScheme extends UserLoginSecurityScheme {
  private static final Logger logger = LoggerFactory.getLogger(FusionAuthUserLoginSecurityScheme.class);
  
  private final FusionAuthConfiguration configuration;
  
  private final HTTPMethod method;
  
  private final HTTPRequest request;
  
  protected String invalidRefererPath = "/ajax/";
  
  protected String validRefererPath = "/admin/";
  
  @Inject
  public FusionAuthUserLoginSecurityScheme(FusionAuthConfiguration paramFusionAuthConfiguration, MVCConfiguration paramMVCConfiguration, UserLoginConstraintsValidator paramUserLoginConstraintsValidator, CSRFProvider paramCSRFProvider, HTTPRequest paramHTTPRequest, HTTPMethod paramHTTPMethod) {
    super(paramMVCConfiguration, paramUserLoginConstraintsValidator, paramCSRFProvider, paramHTTPRequest, paramHTTPMethod);
    this.configuration = paramFusionAuthConfiguration;
    this.method = paramHTTPMethod;
    this.request = paramHTTPRequest;
  }
  
  public void handle(String[] paramArrayOfString) {
    super.handle(paramArrayOfString);
    if (!this.configuration.adminStrictRefererProtectionEnabled())
      return; 
    String str1 = this.request.getPath();
    String str2 = this.request.getQueryString();
    if (str2 != null)
      str1 = str1 + "?" + str1; 
    String str3 = this.request.getHeader("Referer");
    if (str3 == null) {
      if (str1.startsWith("/api/status") || str1.startsWith("/api/prometheus/metrics") || str1.startsWith("/api/system/version"))
        return; 
      throw buildException("Missing Referer.", str1, "", HTTPMethod.GET.is(this.method));
    } 
    URI uRI1 = URI.create(this.request.getBaseURL());
    URI uRI2 = URI.create(str3);
    String str4 = StringTools.defaultIfNull(uRI2.getPath(), "/");
    if (uRI1.getPort() != uRI2.getPort() || 
      !uRI1.getScheme().equalsIgnoreCase(uRI2.getScheme()) || 
      !uRI1.getHost().equalsIgnoreCase(uRI2.getHost()))
      throw buildException("Cross origin request.", str1, str4, HTTPMethod.GET.is(this.method)); 
    if (str4.startsWith(this.invalidRefererPath))
      throw buildException("Invalid Referer.", str1, str4, false); 
    if (str4.startsWith(this.validRefererPath))
      return; 
    throw buildException("Invalid Referer.", str1, str4, HTTPMethod.GET.is(this.method));
  }
  
  private ErrorException buildException(String paramString1, String paramString2, String paramString3, boolean paramBoolean) {
    if (paramBoolean) {
      logger.debug("[{}] {} Requested [{}]. Referer [{}]. Upgrade the request.", new Object[] { this.method, paramString1, paramString2, paramString3 });
      return new InvalidRefererException(paramString2);
    } 
    logger.debug("[{}] {} Requested [{}]. Referer [{}]. Unauthorized.", new Object[] { this.method, paramString1, paramString2, paramString3 });
    return (ErrorException)new UnauthorizedException();
  }
}
