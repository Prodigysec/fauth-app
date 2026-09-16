package io.fusionauth.api.service.messenger;

import java.net.URI;
import org.primeframework.mvc.ErrorException;

public class MessengerException extends ErrorException {
  public final String errorResponse;
  
  public final Integer httpStatus;
  
  public final String messengerName;
  
  public final URI messengerUrl;
  
  public MessengerException(String paramString, URI paramURI, Throwable paramThrowable) {
    super("error", paramThrowable, new Object[0]);
    this.httpStatus = null;
    this.errorResponse = null;
    this.messengerName = paramString;
    this.messengerUrl = paramURI;
  }
  
  public MessengerException(String paramString1, URI paramURI, String paramString2, int paramInt) {
    super("error");
    this.httpStatus = Integer.valueOf(paramInt);
    this.errorResponse = paramString2;
    this.messengerName = paramString1;
    this.messengerUrl = paramURI;
  }
}
