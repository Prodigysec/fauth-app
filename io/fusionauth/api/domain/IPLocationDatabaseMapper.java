package io.fusionauth.api.domain;

import io.fusionauth.api.domain.ip.maxmind.IPLocation;
import io.fusionauth.api.domain.ip.maxmind.IPLocationMetaData;
import java.time.ZonedDateTime;
import java.util.Collection;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface IPLocationDatabaseMapper {
  public static final String SelectDataOrderBySequenceWhere = "SELECT data FROM ip_location_database WHERE last_modified = ? ORDER BY seq";
  
  @Insert({"<script>\nINSERT INTO ip_location_database (data, last_modified, seq)\n    VALUES\n    <foreach collection=\"rows\" item=\"row\" separator=\",\">\n      (#{row.data}, #{row.lastModified}, #{row.seq})\n    </foreach>\n</script>\n"})
  void createIPLocationData(@Param("rows") Collection<IPLocation> paramCollection);
  
  @Insert({"INSERT INTO ip_location_meta_data (digest, last_modified) VALUES (#{digest}, #{lastModified})"})
  void createIPLocationMetaData(IPLocationMetaData paramIPLocationMetaData);
  
  @Delete({"DELETE FROM ip_location_database"})
  void deleteAllIPLocationData();
  
  @Delete({"DELETE FROM ip_location_meta_data"})
  void deleteAllIPLocationMetaData();
  
  @Delete({"DELETE FROM ip_location_database WHERE last_modified = #{lastModified}"})
  void deleteIPLocationData(ZonedDateTime paramZonedDateTime);
  
  @Delete({"DELETE FROM ip_location_meta_data WHERE last_modified = #{lastModified}"})
  void deleteIPLocationMetaData(ZonedDateTime paramZonedDateTime);
  
  @Select({"SELECT digest, last_modified FROM ip_location_meta_data ORDER BY last_modified DESC LIMIT 1;"})
  IPLocationMetaData getLatestIPLocationMetaData();
}
