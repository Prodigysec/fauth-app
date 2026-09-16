package io.fusionauth.webauthn;

public class WebAuthnException extends RuntimeException {
  public WebAuthnException() {}
  
  public WebAuthnException(String paramString) {
    super(paramString);
  }
  
  public WebAuthnException(String paramString, Throwable paramThrowable) {
    super(paramString, paramThrowable);
  }
}
