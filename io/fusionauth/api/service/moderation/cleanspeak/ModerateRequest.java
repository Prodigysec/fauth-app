package io.fusionauth.api.service.moderation.cleanspeak;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ModerateRequest {
  public Content content;
  
  public boolean dryRun;
  
  public ModerationType moderation;
  
  public ModerateRequest() {}
  
  public ModerateRequest(Content paramContent, ModerationType paramModerationType, boolean paramBoolean) {
    this.content = paramContent;
    this.moderation = paramModerationType;
    this.dryRun = paramBoolean;
  }
  
  public static class Content {
    public UUID applicationId;
    
    public ZonedDateTime createInstant;
    
    public String location;
    
    public List<ContentPart> parts = new ArrayList<>();
    
    public String receiverDisplayName;
    
    public UUID receiverId;
    
    public String senderDisplayName;
    
    public UUID senderId;
    
    public Content() {}
    
    public Content(UUID param1UUID1, ZonedDateTime param1ZonedDateTime, String param1String1, String param1String2, UUID param1UUID2, String param1String3, UUID param1UUID3, ContentPart... param1VarArgs) {
      this.applicationId = param1UUID1;
      this.createInstant = param1ZonedDateTime;
      this.location = param1String1;
      this.receiverDisplayName = param1String2;
      this.receiverId = param1UUID2;
      this.senderDisplayName = param1String3;
      this.senderId = param1UUID3;
      Collections.addAll(this.parts, param1VarArgs);
    }
    
    public static class ContentPart {
      public String content;
      
      public String name;
      
      public ContentType type;
      
      public ContentPart() {}
      
      public ContentPart(String param2String) {
        this.content = param2String;
        this.type = ContentType.text;
      }
      
      public ContentPart(String param2String1, ContentType param2ContentType, String param2String2) {
        this.content = param2String2;
        this.name = param2String1;
        this.type = param2ContentType;
      }
    }
  }
  
  public static class ContentPart {
    public String content;
    
    public String name;
    
    public ContentType type;
    
    public ContentPart() {}
    
    public ContentPart(String param1String) {
      this.content = param1String;
      this.type = ContentType.text;
    }
    
    public ContentPart(String param1String1, ContentType param1ContentType, String param1String2) {
      this.content = param1String2;
      this.name = param1String1;
      this.type = param1ContentType;
    }
  }
}
