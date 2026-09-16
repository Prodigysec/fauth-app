package io.fusionauth.api.service.moderation.cleanspeak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ModerateResponse {
  public ContentResponse content;
  
  public FilterAction contentAction;
  
  public ModerationType moderationAction;
  
  public boolean stored;
  
  public ModerateResponse() {}
  
  public ModerateResponse(ContentResponse paramContentResponse, FilterAction paramFilterAction, ModerationType paramModerationType, boolean paramBoolean) {
    this.content = paramContentResponse;
    this.contentAction = paramFilterAction;
    this.moderationAction = paramModerationType;
    this.stored = paramBoolean;
  }
  
  public static class ContentResponse {
    public UUID id;
    
    public List<ContentResponsePart> parts = new ArrayList<>();
    
    public ContentResponse() {}
    
    public ContentResponse(UUID param1UUID, ContentResponsePart... param1VarArgs) {
      this.id = param1UUID;
      Collections.addAll(this.parts, param1VarArgs);
    }
    
    public static class ContentResponsePart {
      public String name;
      
      public String replacement;
      
      public ContentResponsePart() {}
      
      public ContentResponsePart(String param2String1, String param2String2) {
        this.replacement = param2String1;
        this.name = param2String2;
      }
    }
  }
  
  public static class ContentResponsePart {
    public String name;
    
    public String replacement;
    
    public ContentResponsePart() {}
    
    public ContentResponsePart(String param1String1, String param1String2) {
      this.replacement = param1String1;
      this.name = param1String2;
    }
  }
}
