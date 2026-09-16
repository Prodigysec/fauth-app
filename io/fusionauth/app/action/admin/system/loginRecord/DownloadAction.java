package io.fusionauth.app.action.admin.system.loginRecord;

import com.google.inject.Inject;
import com.inversoft.authentication.api.domain.LocalKey;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.RESTClient;
import io.fusionauth.api.domain.guice.FusionAuthLocalClientURL;
import io.fusionauth.api.util.DownloadTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.LoginRecordExportRequest;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.NoOp;
import org.primeframework.mvc.scope.annotation.BrowserActionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoOp
@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class DownloadAction extends IndexAction {
  private static final Logger logger = LoggerFactory.getLogger(DownloadAction.class);
  
  private final String fusionAuthClientURL;
  
  @BrowserActionSession(action = IndexAction.class, name = "s")
  public LoginRecordSearchCriteria criteria;
  
  @Inject
  public DownloadAction(FrontEndSupport paramFrontEndSupport, @FusionAuthLocalClientURL String paramString) {
    super(paramFrontEndSupport);
    this.fusionAuthClientURL = paramString;
  }
  
  public String get() throws IOException {
    String str1 = this.frontEndSupport.messageProvider.getMessage("date-time-seconds-format", new Object[0]);
    LoginRecordExportRequest loginRecordExportRequest = new LoginRecordExportRequest(this.criteria, str1, this.zoneId);
    String str2 = ZonedDateTime.now(this.zoneId).format(DateTimeFormatter.ofPattern(str1));
    String str3 = "Login Record Export " + str2 + ".zip";
    ClientResponse clientResponse = (new RESTClient(InputStream.class, Errors.class)).authorization(LocalKey.KEY).url(this.fusionAuthClientURL).uri("/api/system/login-record/export").readTimeout(120000).bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(loginRecordExportRequest)).successResponseHandler(new DownloadTools.StreamResponseHandler(this.frontEndSupport.response, str3, "application/zip")).errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(Errors.class)).post().go();
    if (!clientResponse.wasSuccessful()) {
      if (clientResponse.exception != null)
        throw new ErrorException("error", clientResponse.exception, new Object[0]); 
      logger.error("Failed to call the /api/system/login-record/export API. Response code: [" + clientResponse.status + "]\n" + String.valueOf(clientResponse.errorResponse));
      return "error";
    } 
    writeAuditLog("Login records were exported.");
    return "success";
  }
}
