package io.fusionauth.api.service.lambda.graal;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;
import io.fusionauth.api.domain.api.RequestContext;
import io.fusionauth.api.service.lambda.graal.domain.LambdaConsole;
import io.fusionauth.api.service.lambda.graal.domain.Response;
import io.fusionauth.api.service.reactor.TextBodyHandler;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

public class Fetch implements ProxyExecutable {
  private final LambdaConsole console;
  
  private final UUID lambdaId;
  
  private final String lambdaName;
  
  private final boolean licensed;
  
  private final MetricRegistry metricRegistry;
  
  public Fetch(LambdaConsole paramLambdaConsole, UUID paramUUID, String paramString, boolean paramBoolean, MetricRegistry paramMetricRegistry) {
    this.console = paramLambdaConsole;
    this.lambdaId = paramUUID;
    this.lambdaName = paramString;
    this.licensed = paramBoolean;
    this.metricRegistry = paramMetricRegistry;
  }
  
  public Object execute(Value... paramVarArgs) {
    Meter meter1 = this.metricRegistry.meter("lambda.[*].http-connect.failures");
    Meter meter2 = this.metricRegistry.meter("lambda.[" + String.valueOf(this.lambdaId) + "].http-connect.failures");
    if (!this.licensed)
      return buildErrorResponse(402, "This function is part of the Advanced Lambdas feature. Please contact FusionAuth for more information about this feature."); 
    if (paramVarArgs.length == 0)
      throw new IllegalArgumentException("Invalid call to fetch(). You must supply at least one argument, the first of which must be a URL."); 
    Timer.Context context = this.metricRegistry.timer("lambda.[*].http-connect").time();
    try {
      Timer.Context context1 = this.metricRegistry.timer("lambda.[" + String.valueOf(this.lambdaId) + "].http-connect").time();
      try {
        String str = paramVarArgs[0].asString();
        RequestContext requestContext = RequestContext.get();
        if (requestContext != null) {
          URI uRI = URI.create(str);
          if (requestContext.path.equals(uRI.getPath())) {
            String str1 = String.format("Lambda invocation error.\n\nId: %s\nName: %s\n\nCause: Illegal recursive request.\nDescription: You cannot call the API from a lambda that is currently invoking this lambda. That would be bad.\nRequested URL: %s", new Object[] { this.lambdaId, this.lambdaName, str });
            EventLogHelper.create(new EventLog(EventLogType.Debug, str1));
            Response response = buildErrorResponse(400, str1.replace("\n", ""));
            if (context1 != null)
              context1.close(); 
            if (context != null)
              context.close(); 
            return response;
          } 
        } 
        if (this.console.isDebugEnabled()) {
          debug("Begin HTTP request debug");
          debug("Arguments:");
          for (byte b = 0; b < paramVarArgs.length; b++)
            debug("" + b + 1 + ". " + b + 1); 
          debug("");
        } 
        RESTClient rESTClient = (new RESTClient(String.class, String.class)).url(str).successResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler()).errorResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler());
        if (paramVarArgs.length >= 2) {
          Value value1 = paramVarArgs[1];
          String str1 = safeGetValueAsStringWithDefault(value1, "method", "GET").toUpperCase();
          rESTClient.method(str1);
          debug(str1 + " " + str1);
          Value value2 = safeGetMember(value1, "headers");
          if (value2 != null)
            for (String str3 : value2.getMemberKeys()) {
              Value value = value2.getMember(str3);
              List list = value.hasArrayElements() ? (List)value.as(List.class) : List.of(value);
              for (String str4 : list) {
                String str5 = (str4 instanceof String) ? str4 : ((Value)str4).asString();
                rESTClient.header(str3, str5);
                debug(str3 + ": " + str3);
              } 
            }  
          String str2 = safeGetValueAsString(value1, "body");
          if (str2 != null) {
            rESTClient.bodyHandler(new TextBodyHandler(null, str2));
            debug(str2);
          } 
          Value value3 = safeGetMember(value1, "connectTimeout");
          if (value3 != null)
            rESTClient.connectTimeout(value3.asInt()); 
          Value value4 = safeGetMember(value1, "readTimeout");
          if (value4 != null)
            rESTClient.readTimeout(value4.asInt()); 
        } else {
          debug("GET " + str);
          rESTClient.get();
        } 
        long l1 = System.currentTimeMillis();
        Response response1 = new Response(rESTClient.go());
        long l2 = System.currentTimeMillis() - l1;
        int i = 0;
        try {
          i = Integer.parseInt(response1.getMember("status").toString());
        } catch (Exception exception) {}
        if (i < 200 || i > 299) {
          meter1.mark();
          meter2.mark();
        } 
        if (this.console.isDebugEnabled()) {
          debug("");
          debug("Request duration: " + l2 + " ms");
          debug("Response status: " + response1.getMember("status").toString());
          debug("Response headers: " + response1.getMember("headers").toString());
          debug("Response body: \n" + response1.getMember("body").toString());
          debug("End HTTP request debug\n");
        } 
        Response response2 = response1;
        if (context1 != null)
          context1.close(); 
        if (context != null)
          context.close(); 
        return response2;
      } catch (Throwable throwable) {
        if (context1 != null)
          try {
            context1.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (Throwable throwable) {
      if (context != null)
        try {
          context.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  private Response buildErrorResponse(int paramInt, String paramString) {
    ClientResponse<String, String> clientResponse = new ClientResponse();
    clientResponse.status = paramInt;
    clientResponse





      
      .errorResponse = "{\n  \"generalErrors\": [{\n    \"code\": \"[LambdaFetchRequestFailed]\",\n    \"message\": \"{{message}}\"\n  }]\n}\n".replace("{{message}}", paramString);
    clientResponse.headers.put("Content-Type", List.of("application/json; charset=utf-8"));
    clientResponse.headers.put("Content-Length", List.of(String.valueOf((((String)clientResponse.errorResponse).getBytes(StandardCharsets.UTF_8)).length)));
    return new Response(clientResponse);
  }
  
  private void debug(String paramString) {
    if (this.console.isDebugEnabled())
      this.console.debug.append(DateTimeFormatter.ofPattern("M/d/yyyy hh:mm:ss a z").format(ZonedDateTime.now(ZoneOffset.UTC)))
        .append(" ")
        .append(paramString)
        .append("\n"); 
  }
  
  private Value safeGetMember(Value paramValue, String paramString) {
    try {
      return paramValue.getMember(paramString);
    } catch (UnsupportedOperationException unsupportedOperationException) {
      throw new IllegalArgumentException("Invalid call to fetch(). The second parameter must be an Object");
    } 
  }
  
  private String safeGetValueAsString(Value paramValue, String paramString) {
    Value value = safeGetMember(paramValue, paramString);
    return (value != null) ? value.asString() : null;
  }
  
  private String safeGetValueAsStringWithDefault(Value paramValue, String paramString1, String paramString2) {
    String str = safeGetValueAsString(paramValue, paramString1);
    return (str != null) ? str : paramString2;
  }
}
