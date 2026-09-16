package io.fusionauth.api.service.event;

public class WebhookTransactionException extends WebhookException {
  public WebhookTransactionException() {
    super("webhook-transaction-failed");
  }
}
