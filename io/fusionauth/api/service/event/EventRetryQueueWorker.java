package io.fusionauth.api.service.event;

import com.google.inject.Inject;
import io.fusionauth.domain.KafkaConfiguration;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventRetryQueueWorker implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(EventRetryQueueWorker.class);
  
  private final EventRetryQueue queue;
  
  @Inject
  public EventRetryQueueWorker(EventRetryQueue paramEventRetryQueue) {
    this.queue = paramEventRetryQueue;
  }
  
  public void run() {
    Collection<EventRetryQueue.RetryPayload> collection = this.queue.popAll();
    collection.forEach(paramRetryPayload -> {
          boolean bool = paramRetryPayload.retry();
          if (!bool) {
            paramRetryPayload.retryCount++;
            if (paramRetryPayload.retryCount < 3) {
              this.queue.add(paramRetryPayload);
            } else {
              logError(paramRetryPayload);
            } 
          } 
        });
  }
  
  private void logError(EventRetryQueue.RetryPayload paramRetryPayload) {
    EventSender eventSender = paramRetryPayload.eventSender;
    if (eventSender instanceof WebhookEventSender) {
      WebhookEventSender webhookEventSender = (WebhookEventSender)eventSender;
      logger.error("Unable to send notification to the notification server [{}] after 3 attempts. Throwing the notification out. Webhook definition\n{}", webhookEventSender.webhook.url, webhookEventSender.webhook);
    } else {
      eventSender = paramRetryPayload.eventSender;
      if (eventSender instanceof KafkaEventSender) {
        KafkaEventSender kafkaEventSender = (KafkaEventSender)eventSender;
        KafkaConfiguration kafkaConfiguration = kafkaEventSender.kafkaService.getConfiguration();
        logger.error("Unable to send event to Kafka topic [{}] on server [{}] after 3 attempts. Throwing the notification out.", kafkaConfiguration.defaultTopic, kafkaConfiguration.producer.get("bootstrap.servers"));
      } else {
        throw new IllegalStateException("Unknown event receiver [" + paramRetryPayload.eventSender.getClass().getSimpleName() + "]");
      } 
    } 
  }
}
