package io.fusionauth.api.domain.ip.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;

public final class Postal {
  public final String code;
  
  public final Integer confidence;
  
  public Postal() {
    this.code = null;
    this.confidence = null;
  }
  
  @MaxMindDbConstructor
  public Postal(@MaxMindDbParameter(name = "code") String paramString, @MaxMindDbParameter(name = "confidence") Integer paramInteger) {
    this.code = paramString;
    this.confidence = paramInteger;
  }
}
