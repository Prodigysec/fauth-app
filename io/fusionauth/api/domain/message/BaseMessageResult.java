package io.fusionauth.api.domain.message;

import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseMessageResult {
  public final Map<String, ParseException> parseErrors = new HashMap<>();
  
  public final Map<String, TemplateException> renderErrors = new HashMap<>();
  
  public String error;
  
  public int status;
  
  public BaseMessageResult() {}
  
  public BaseMessageResult(Map<String, ParseException> paramMap, Map<String, TemplateException> paramMap1) {
    this.parseErrors.putAll(paramMap);
    this.renderErrors.putAll(paramMap1);
  }
  
  public boolean wasSuccessful() {
    return (this.parseErrors.isEmpty() && this.renderErrors.isEmpty());
  }
}
