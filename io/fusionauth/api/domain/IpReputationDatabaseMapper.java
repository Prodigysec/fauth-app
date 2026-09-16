package io.fusionauth.api.domain;

import java.time.ZonedDateTime;
import java.util.Collection;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface IpReputationDatabaseMapper {
  public static final String SelectDataOrderBySequenceWhere = "SELECT data FROM ip_reputation_data WHERE last_modified = ? ORDER BY seq";
  
  @Insert({"<script>\nINSERT INTO ip_reputation_data (data, last_modified, seq)\n    VALUES\n    <foreach collection=\"rows\" item=\"row\" separator=\",\">\n      (#{row.data}, #{row.lastModified}, #{row.seq})\n    </foreach>\n</script>\n"})
  void createIpReputationData(@Param("rows") Collection<SequencedData> paramCollection);
  
  @Insert({"INSERT INTO ip_reputation_meta_data (digest, last_modified) VALUES (#{digest}, #{lastModified})"})
  void createIpReputationMetaData(SequencedMetaData paramSequencedMetaData);
  
  @Delete({"DELETE FROM ip_reputation_data"})
  void deleteAllIpReputationData();
  
  @Delete({"DELETE FROM ip_reputation_meta_data"})
  void deleteAllIpReputationMetaData();
  
  @Delete({"DELETE FROM ip_reputation_data WHERE last_modified = #{lastModified}"})
  void deleteIpReputationData(ZonedDateTime paramZonedDateTime);
  
  @Delete({"DELETE FROM ip_reputation_meta_data WHERE last_modified = #{lastModified}"})
  void deleteIpReputationMetaData(ZonedDateTime paramZonedDateTime);
  
  @Select({"SELECT digest, last_modified FROM ip_reputation_meta_data ORDER BY last_modified DESC LIMIT 1;"})
  SequencedMetaData getLatestIpReputationMetaData();
}
