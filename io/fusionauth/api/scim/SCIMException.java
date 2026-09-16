package io.fusionauth.api.scim;

import com.inversoft.error.Errors;
import io.fusionauth.domain.Buildable;
import org.primeframework.mvc.ErrorException;

public class SCIMException extends ErrorException implements Buildable<SCIMException> {
  public final String detail;
  
  public final Errors errors;
  
  public final boolean lookupErrorMessages;
  
  public SCIMException(String paramString1, String paramString2) {
    super(paramString1);
    this.detail = paramString2;
    this.errors = null;
    this.lookupErrorMessages = false;
  }
  
  public SCIMException(String paramString1, Throwable paramThrowable, String paramString2) {
    super(paramString1, paramThrowable, new Object[0]);
    this.detail = paramString2;
    this.errors = null;
    this.lookupErrorMessages = false;
  }
  
  public SCIMException(String paramString, Throwable paramThrowable) {
    super(paramString, paramThrowable, new Object[0]);
    this.detail = null;
    this.errors = null;
    this.lookupErrorMessages = false;
  }
  
  public SCIMException(ErrorException paramErrorException, String paramString) {
    super(paramErrorException.resultCode, (Throwable)paramErrorException, new Object[0]);
    this.detail = paramString;
    this.errors = null;
    this.lookupErrorMessages = false;
  }
  
  public SCIMException(ErrorException paramErrorException) {
    super(paramErrorException.resultCode, (Throwable)paramErrorException, new Object[0]);
    this.detail = null;
    this.errors = null;
    this.lookupErrorMessages = false;
  }
  
  public SCIMException(String paramString, Errors paramErrors) {
    super(paramString);
    this.detail = null;
    this.errors = paramErrors;
    this.lookupErrorMessages = false;
  }
  
  public SCIMException(String paramString, Errors paramErrors, boolean paramBoolean) {
    super(paramString);
    this.detail = null;
    this.errors = paramErrors;
    this.lookupErrorMessages = paramBoolean;
  }
  
  public SCIMException(String paramString, Object... paramVarArgs) {
    super(paramString, paramVarArgs);
    this.detail = null;
    this.errors = null;
    this.lookupErrorMessages = false;
  }
}
