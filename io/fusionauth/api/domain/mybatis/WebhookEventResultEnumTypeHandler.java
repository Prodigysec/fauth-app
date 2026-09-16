package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.WebhookEventResult;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class WebhookEventResultEnumTypeHandler extends EnumOrdinalTypeHandler<WebhookEventResult> {
  public WebhookEventResultEnumTypeHandler() {
    super(WebhookEventResult.class);
  }
}
