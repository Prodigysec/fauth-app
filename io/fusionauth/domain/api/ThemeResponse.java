package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Theme;
import java.util.List;

public class ThemeResponse {
  public Theme theme;
  
  public List<Theme> themes;
  
  @JacksonConstructor
  public ThemeResponse() {}
  
  public ThemeResponse(Theme paramTheme) {
    this.theme = paramTheme;
  }
  
  public ThemeResponse(List<Theme> paramList) {
    this.themes = paramList;
  }
}
