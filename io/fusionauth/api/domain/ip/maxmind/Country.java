package io.fusionauth.api.domain.ip.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import java.util.Map;

public class Country extends AbstractNamedRecord {
  public final Integer confidence;
  
  public final Boolean isInEuropeanUnion;
  
  public final String isoCode;
  
  public Country() {
    this.confidence = null;
    this.isInEuropeanUnion = Boolean.valueOf(false);
    this.isoCode = null;
  }
  
  @MaxMindDbConstructor
  public Country(@MaxMindDbParameter(name = "confidence") Integer paramInteger, @MaxMindDbParameter(name = "geoname_id") Long paramLong, @MaxMindDbParameter(name = "is_in_european_union") Boolean paramBoolean, @MaxMindDbParameter(name = "iso_code") String paramString, @MaxMindDbParameter(name = "names") Map<String, String> paramMap) {
    super(paramLong, paramMap);
    this.confidence = paramInteger;
    this.isInEuropeanUnion = paramBoolean;
    this.isoCode = paramString;
  }
}
