package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.Integrations;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

public class IntegrationsResultHandler implements ResultHandler<Integrations> {
  public Integrations integrations;
  
  public void handleResult(ResultContext paramResultContext) {
    Object object = paramResultContext.getResultObject();
    if (object == null) {
      this.integrations = new Integrations();
    } else {
      this.integrations = (Integrations)object;
    } 
  }
}
