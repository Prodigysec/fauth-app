package io.fusionauth.api.domain.ip.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import java.util.Map;

public final class RepresentedCountry extends Country {
  public final String type;
  
  public RepresentedCountry() {
    this.type = null;
  }
  
  @MaxMindDbConstructor
  public RepresentedCountry(@MaxMindDbParameter(name = "confidence") Integer paramInteger, @MaxMindDbParameter(name = "geoname_id") Long paramLong, @MaxMindDbParameter(name = "is_in_european_union") Boolean paramBoolean, @MaxMindDbParameter(name = "iso_code") String paramString1, @MaxMindDbParameter(name = "names") Map<String, String> paramMap, @MaxMindDbParameter(name = "type") String paramString2) {
    super(paramInteger, paramLong, paramBoolean, paramString1, paramMap);
    this.type = paramString2;
  }
}
