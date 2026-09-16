package io.fusionauth.api.service.reactor;

import com.inversoft.rest.RESTClient;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class TextBodyHandler implements RESTClient.BodyHandler {
  public String contentType;
  
  public String request;
  
  public TextBodyHandler(String paramString1, String paramString2) {
    this.contentType = paramString1;
    this.request = paramString2;
  }
  
  public void accept(OutputStream paramOutputStream) throws IOException {
    if (this.request != null) {
      paramOutputStream.write(this.request.getBytes(StandardCharsets.UTF_8));
      paramOutputStream.flush();
    } 
  }
  
  public byte[] getBody() {
    throw new UnsupportedOperationException();
  }
  
  public Object getBodyObject() {
    return this.request;
  }
  
  public void setHeaders(HttpURLConnection paramHttpURLConnection) {
    if (this.contentType != null)
      paramHttpURLConnection.addRequestProperty("Content-Type", this.contentType); 
    paramHttpURLConnection.addRequestProperty("Content-Length", "" + this.request.length());
  }
}
