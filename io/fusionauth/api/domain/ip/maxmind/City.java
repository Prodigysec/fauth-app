package io.fusionauth.api.domain.ip.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import java.util.Map;

public final class City extends AbstractNamedRecord {
  public final Integer confidence;
  
  public City() {
    this.confidence = null;
  }
  
  @MaxMindDbConstructor
  public City(@MaxMindDbParameter(name = "confidence") Integer paramInteger, @MaxMindDbParameter(name = "geoname_id") Long paramLong, @MaxMindDbParameter(name = "names") Map<String, String> paramMap) {
    super(paramLong, paramMap);
    this.confidence = paramInteger;
  }
}
