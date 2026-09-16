package io.fusionauth.api.domain.ip.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import java.util.Map;

public final class Subdivision extends AbstractNamedRecord {
  public final Integer confidence;
  
  public final String isoCode;
  
  @MaxMindDbConstructor
  public Subdivision(@MaxMindDbParameter(name = "confidence") Integer paramInteger, @MaxMindDbParameter(name = "geoname_id") Long paramLong, @MaxMindDbParameter(name = "iso_code") String paramString, @MaxMindDbParameter(name = "names") Map<String, String> paramMap) {
    super(paramLong, paramMap);
    this.confidence = paramInteger;
    this.isoCode = paramString;
  }
}
