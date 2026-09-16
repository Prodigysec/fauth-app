package io.fusionauth.api.domain.ip.maxmind;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractNamedRecord {
  public final Long geoNameId;
  
  public final Map<String, String> names;
  
  AbstractNamedRecord() {
    this.geoNameId = null;
    this.names = Map.of();
  }
  
  AbstractNamedRecord(Long paramLong, Map<String, String> paramMap) {
    this.geoNameId = paramLong;
    this.names = (paramMap != null) ? paramMap : new HashMap<>();
  }
  
  public String getName() {
    Locale locale = Locale.getDefault();
    String str = this.names.get(locale.toString());
    if (str == null)
      str = this.names.get(Locale.ENGLISH.toString()); 
    return str;
  }
}
