package io.fusionauth.api.domain;

import java.time.ZonedDateTime;
import java.util.Collection;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserAgentReputationDatabaseMapper {
  public static final String SelectDataOrderBySequenceWhere = "SELECT data FROM user_agent_reputation_data WHERE last_modified = ? ORDER BY seq";
  
  @Insert({"<script>\nINSERT INTO user_agent_reputation_data (data, last_modified, seq)\n    VALUES\n    <foreach collection=\"rows\" item=\"row\" separator=\",\">\n      (#{row.data}, #{row.lastModified}, #{row.seq})\n    </foreach>\n</script>\n"})
  void createUserAgentReputationData(@Param("rows") Collection<SequencedData> paramCollection);
  
  @Insert({"INSERT INTO user_agent_reputation_meta_data (digest, last_modified) VALUES (#{digest}, #{lastModified})"})
  void createUserAgentReputationMetaData(SequencedMetaData paramSequencedMetaData);
  
  @Delete({"DELETE FROM user_agent_reputation_data"})
  void deleteAllUserAgentReputationData();
  
  @Delete({"DELETE FROM user_agent_reputation_meta_data"})
  void deleteAllUserAgentReputationMetaData();
  
  @Delete({"DELETE FROM user_agent_reputation_data WHERE last_modified = #{lastModified}"})
  void deleteUserAgentReputationData(ZonedDateTime paramZonedDateTime);
  
  @Delete({"DELETE FROM user_agent_reputation_meta_data WHERE last_modified = #{lastModified}"})
  void deleteUserAgentReputationMetaData(ZonedDateTime paramZonedDateTime);
  
  @Select({"SELECT digest, last_modified FROM user_agent_reputation_meta_data ORDER BY last_modified DESC LIMIT 1;"})
  SequencedMetaData getLatestUserAgentReputationMetaData();
}
