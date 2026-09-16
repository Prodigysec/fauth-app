package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Theme;
import java.util.UUID;

public class ThemeRequest {
  public UUID sourceThemeId;
  
  public Theme theme;
  
  @JacksonConstructor
  public ThemeRequest() {}
  
  public ThemeRequest(Theme paramTheme) {
    this.theme = paramTheme;
  }
}
