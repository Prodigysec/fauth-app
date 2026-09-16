package io.fusionauth.app.service.system;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.util.Pair;
import io.fusionauth.api.domain.FusionAuthNodeMapper;
import io.fusionauth.api.domain.guice.FusionAuthInternalAPIKey;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.api.util.DownloadTools;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.api.SystemLogsExportRequest;
import io.fusionauth.http.server.HTTPResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.security.UnauthenticatedException;
import org.primeframework.mvc.util.EncodingUtils;

public class DefaultSystemLogFrontendService implements SystemLogFrontendService {
  private final APIKey internalAPIKey;
  
  private final NodeService nodeService;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  @Inject
  public DefaultSystemLogFrontendService(@FusionAuthInternalAPIKey APIKey paramAPIKey, NodeService paramNodeService, ProxyInfoSupplier paramProxyInfoSupplier) {
    this.internalAPIKey = paramAPIKey;
    this.nodeService = paramNodeService;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
  }
  
  public void downloadLogs(String paramString1, HTTPResponse paramHTTPResponse, String paramString2, ZoneId paramZoneId, boolean paramBoolean) {
    paramHTTPResponse.setStatus(200);
    paramHTTPResponse.setContentType("application/zip");
    paramHTTPResponse.setHeader("Content-Disposition", "attachment; filename=\"" + EncodingUtils.escapedQuotedString(paramString1) + "\"; filename*=UTF-8''" + EncodingUtils.rfc5987_encode(paramString1));
    try {
      ZipOutputStream zipOutputStream = new ZipOutputStream(paramHTTPResponse.getOutputStream());
      try {
        zipOutputStream.setLevel(9);
        zipOutputStream.putNextEntry(new ZipEntry("logs/"));
        for (FusionAuthNodeMapper.FusionAuthNode fusionAuthNode : this.nodeService.retrieveAll()) {
          zipOutputStream.putNextEntry(new ZipEntry("logs/" + String.valueOf(fusionAuthNode.id) + ".zip"));
          SystemLogsExportRequest systemLogsExportRequest = new SystemLogsExportRequest(paramString2, paramZoneId, -1, paramBoolean);
          ClientResponse clientResponse = (new RESTClient(InputStream.class, Errors.class)).authorization(this.internalAPIKey.key).url(fusionAuthNode.url).uri("/api/system/log/export").readTimeout(120000).bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(systemLogsExportRequest)).successResponseHandler(new DownloadTools.OutputStreamResultHandler(zipOutputStream)).errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(Errors.class)).proxy((ProxyInfo)this.proxyInfoSupplier.get()).post().go();
          if (clientResponse.wasSuccessful()) {
            zipOutputStream.closeEntry();
            continue;
          } 
          String str = "Failed retrieve logs from node Id [" + String.valueOf(fusionAuthNode.id) + "] from URL [" + fusionAuthNode.url + "].";
          if (clientResponse.exception != null) {
            EventLogHelper.create(new EventLog(EventLogType.Error, str, clientResponse.exception));
          } else if (clientResponse.errorResponse != null) {
            EventLogHelper.create(new EventLog(EventLogType.Error, str + "\nError response:" + str));
          } else {
            EventLogHelper.create(new EventLog(EventLogType.Error, str));
          } 
          if (clientResponse.status == 401)
            throw new UnauthenticatedException(); 
          throw new ErrorException("error", clientResponse.exception, new Object[0]);
        } 
        zipOutputStream.close();
      } catch (Throwable throwable) {
        try {
          zipOutputStream.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (IOException iOException) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to retrieve logs.", iOException));
      throw new ErrorException("error", iOException, new Object[0]);
    } 
  }
  
  public SystemLogFrontendService.SystemLogResult retrieveLogsByNodeId(UUID paramUUID, int paramInt) throws IOException {
    List<FusionAuthNodeMapper.FusionAuthNode> list = this.nodeService.retrieveAll();
    SystemLogFrontendService.SystemLogResult systemLogResult = new SystemLogFrontendService.SystemLogResult();
    systemLogResult.node = list.stream().filter(paramFusionAuthNode -> paramFusionAuthNode.id.equals(paramUUID)).findFirst().orElse(null);
    systemLogResult.nodeCount = list.size();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(65536);
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
    ClientResponse clientResponse = (new RESTClient(InputStream.class, Errors.class)).authorization(this.internalAPIKey.key).url(systemLogResult.node.url).uri("/api/system/log/export").urlParameter("lastNBytes", Integer.valueOf(paramInt)).readTimeout(120000).successResponseHandler(new DownloadTools.OutputStreamResultHandler(bufferedOutputStream)).errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(Errors.class)).proxy((ProxyInfo)this.proxyInfoSupplier.get()).post().go();
    if (!clientResponse.wasSuccessful())
      throw new ErrorException("error", clientResponse.exception, new Object[0]); 
    ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), StandardCharsets.UTF_8);
    try {
      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        if (zipEntry.isDirectory())
          continue; 
        String str = DownloadTools.readFileFromInputStream(zipInputStream);
        if (str.length() > 0)
          systemLogResult.logs.add(new Pair(zipEntry.getName().replace("logs/", ""), str)); 
      } 
      zipInputStream.close();
    } catch (Throwable throwable) {
      try {
        zipInputStream.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    return systemLogResult;
  }
}
