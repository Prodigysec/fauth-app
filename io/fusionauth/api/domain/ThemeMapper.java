package io.fusionauth.api.domain;

import io.fusionauth.domain.Theme;
import io.fusionauth.domain.search.ThemeSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;

public interface ThemeMapper {
  void create(Theme paramTheme);
  
  int delete(@Param("id") UUID paramUUID);
  
  @ResultMap({"Theme"})
  List<Theme> retrieveAll();
  
  List<Theme> retrieveByCriteria(ThemeSearchCriteria paramThemeSearchCriteria);
  
  @ResultMap({"Theme"})
  Theme retrieveById(@Param("id") UUID paramUUID);
  
  int retrieveCountByCriteria(ThemeSearchCriteria paramThemeSearchCriteria);
  
  @ResultMap({"Theme"})
  Theme retrieveExistingByName(@Param("id") UUID paramUUID, @Param("name") String paramString);
  
  void update(Theme paramTheme);
}
