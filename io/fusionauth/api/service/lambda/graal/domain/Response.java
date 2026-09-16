package io.fusionauth.api.service.lambda.graal.domain;

import com.inversoft.rest.ClientResponse;
import org.graalvm.polyglot.Value;

public class Response extends AbstractProxyObject {
  public Response(ClientResponse<String, String> paramClientResponse) {
    this.members.put("body", Value.asValue(paramClientResponse.wasSuccessful() ? 
          paramClientResponse.successResponse : 
          paramClientResponse.errorResponse));
    this.members.put("headers", new HeadersObject(paramClientResponse.headers));
    this.members.put("status", Value.asValue(Short.valueOf((short)paramClientResponse.status)));
  }
}
