package io.fusionauth.api.domain;

import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface KickstartMapper {
  @Insert({"INSERT INTO kickstart_files (id, kickstart, name)\nVALUES (#{id}, #{kickstart}, #{name})\n"})
  void createKickstart(KickstartFile paramKickstartFile);
  
  @Select({"SELECT * FROM kickstart_files"})
  List<KickstartFile> retrieveAll();
  
  @Select({"SELECT * FROM kickstart_files WHERE id = #{kickstartId}"})
  KickstartFile retrieveById(UUID paramUUID);
}
