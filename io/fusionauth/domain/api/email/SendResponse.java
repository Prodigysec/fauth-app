package io.fusionauth.domain.api.email;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SendResponse {
  public Map<String, EmailTemplateErrors> anonymousResults;
  
  public Map<UUID, EmailTemplateErrors> results;
  
  public static class EmailTemplateErrors {
    public final Map<String, String> parseErrors = new HashMap<>();
    
    public final Map<String, String> renderErrors = new HashMap<>();
  }
}
