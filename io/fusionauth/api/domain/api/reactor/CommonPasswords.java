package io.fusionauth.api.domain.api.reactor;

import com.inversoft.json.JacksonConstructor;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommonPasswords {
  public ZonedDateTime lastUpdateInstant;
  
  public List<String> passwords = new ArrayList<>();
  
  @JacksonConstructor
  public CommonPasswords() {}
  
  public CommonPasswords(List<String> paramList, ZonedDateTime paramZonedDateTime) {
    this.passwords.addAll(paramList);
    this.lastUpdateInstant = paramZonedDateTime;
  }
}
