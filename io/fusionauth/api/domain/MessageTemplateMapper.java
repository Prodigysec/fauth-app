package io.fusionauth.api.domain;

import io.fusionauth.domain.message.MessageTemplate;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

public interface MessageTemplateMapper {
  void create(@Param("template") MessageTemplate paramMessageTemplate);
  
  int delete(@Param("template") MessageTemplate paramMessageTemplate);
  
  List<MessageTemplate> retrieveAll();
  
  MessageTemplate retrieveById(@Param("id") UUID paramUUID);
  
  MessageTemplate retrieveByName(@Param("name") String paramString);
  
  int update(@Param("template") MessageTemplate paramMessageTemplate);
}
