package io.fusionauth.client;

import com.inversoft.error.Errors;

public class FusionAuthClientException extends RuntimeException {
  public final Errors errors;
  
  public FusionAuthClientException(String paramString) {
    super(paramString);
    this.errors = null;
  }
  
  public FusionAuthClientException(Errors paramErrors) {
    this.errors = paramErrors;
  }
  
  public FusionAuthClientException(Throwable paramThrowable) {
    super(paramThrowable);
    this.errors = null;
  }
}
