package io.fusionauth.api.domain.ip.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import java.util.Map;

public final class Continent extends AbstractNamedRecord {
  public final String code;
  
  public Continent() {
    this.code = null;
  }
  
  @MaxMindDbConstructor
  public Continent(@MaxMindDbParameter(name = "code") String paramString, @MaxMindDbParameter(name = "geoname_id") Long paramLong, @MaxMindDbParameter(name = "names") Map<String, String> paramMap) {
    super(paramLong, paramMap);
    this.code = paramString;
  }
}
