package io.fusionauth.api.service.messaging;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.cache.Cache;
import com.inversoft.json.ToString;
import com.inversoft.util.ThrowingConsumer;
import io.fusionauth.api.service.event.EventSenderResult;
import io.fusionauth.api.service.event.WebhookEventLogService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Integration;
import io.fusionauth.domain.KafkaConfiguration;
import io.fusionauth.domain.WebhookAttemptLog;
import io.fusionauth.domain.WebhookCallResponse;
import io.fusionauth.domain.event.BaseEvent;
import io.fusionauth.domain.event.EventRequest;
import java.io.Closeable;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultKafkaService implements KafkaService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultKafkaService.class);
  
  private final Cache<String, Integration> integrationCache;
  
  private final KafkaProducerProvider kafkaProducerProvider;
  
  private final MetricRegistry metricRegistry;
  
  private final ObjectMapper objectMapper;
  
  private final WebhookEventLogService webhookEventLogService;
  
  @Inject
  public DefaultKafkaService(Cache<String, Integration> paramCache, KafkaProducerProvider paramKafkaProducerProvider, MetricRegistry paramMetricRegistry, @Named("DatabaseObjectMapper") ObjectMapper paramObjectMapper, WebhookEventLogService paramWebhookEventLogService) {
    this.integrationCache = paramCache;
    this.kafkaProducerProvider = paramKafkaProducerProvider;
    this.metricRegistry = paramMetricRegistry;
    this.objectMapper = paramObjectMapper;
    this.webhookEventLogService = paramWebhookEventLogService;
  }
  
  public KafkaConfiguration getConfiguration() {
    return (KafkaConfiguration)this.integrationCache.get("kafka");
  }
  
  public boolean isEnabled() {
    return (getConfiguration()).enabled;
  }
  
  public <T extends BaseEvent> EventSenderResult sendEvent(T paramT) {
    String str1;
    try {
      str1 = this.objectMapper.writeValueAsString(new EventRequest((BaseEvent)paramT));
    } catch (JsonProcessingException jsonProcessingException) {
      throw new RuntimeException(jsonProcessingException);
    } 
    String str2 = (getConfiguration()).defaultTopic;
    long l = ((BaseEvent)paramT).createInstant.toInstant().toEpochMilli();
    KafkaService.KafkaSendResult kafkaSendResult = sendMessage(paramT, new ProducerRecord(str2, null, Long.valueOf(l), null, str1, null));
    return new EventSenderResult(kafkaSendResult.exception, kafkaSendResult.message, null, -1, kafkaSendResult.success);
  }
  
  public KafkaService.KafkaSendResult sendMessage(KafkaProducer<String, String> paramKafkaProducer, String paramString1, String paramString2) {
    return send(null, (Producer<String, String>)paramKafkaProducer, new ProducerRecord(paramString1, paramString2));
  }
  
  public <T extends BaseEvent> void testConfiguration(KafkaConfiguration paramKafkaConfiguration, T paramT) {
    String str;
    if (paramKafkaConfiguration.producer.containsKey("bootstrap.servers"))
      doTry(() -> (new KafkaConsumerBuilder(paramKafkaConfiguration.producer)).testMode().build(), paramKafkaConsumer -> assertTopicExists(paramKafkaConsumer.listTopics(), paramKafkaConfiguration.defaultTopic)); 
    if (((BaseEvent)paramT).id == null)
      ((BaseEvent)paramT).id = UUID.randomUUID(); 
    ((BaseEvent)paramT).createInstant = ZonedDateTime.now(ZoneOffset.UTC);
    try {
      str = this.objectMapper.writeValueAsString(paramT);
    } catch (JsonProcessingException jsonProcessingException) {
      throw new RuntimeException(jsonProcessingException);
    } 
    long l = ((BaseEvent)paramT).createInstant.toInstant().toEpochMilli();
    doTry(() -> (new KafkaProducerBuilder(paramKafkaConfiguration.producer)).testMode().build(), paramKafkaProducer -> paramKafkaProducer.send(new ProducerRecord(paramKafkaConfiguration.defaultTopic, null, Long.valueOf(paramLong), null, paramString, null)).get());
  }
  
  private void assertTopicExists(Map<String, List<PartitionInfo>> paramMap, String paramString) {
    if (!paramMap.containsKey(paramString))
      throw new KafkaProducerException("Topic [" + paramString + "] not found."); 
  }
  
  private <T extends Closeable> void doTry(Supplier<T> paramSupplier, ThrowingConsumer<T> paramThrowingConsumer) {
    Closeable closeable = null;
    try {
      closeable = (Closeable)paramSupplier.get();
      paramThrowingConsumer.accept(closeable);
    } catch (KafkaProducerException kafkaProducerException) {
      throw kafkaProducerException;
    } catch (ExecutionException|InterruptedException|org.apache.kafka.common.KafkaException executionException) {
      throw new KafkaProducerException(executionException, new Object[0]);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    } finally {
      if (closeable != null)
        if (closeable instanceof KafkaConsumer) {
          ((KafkaConsumer)closeable).close(Duration.of(500L, ChronoUnit.MILLIS));
        } else if (closeable instanceof KafkaProducer) {
          ((KafkaProducer)closeable).close(Duration.of(500L, ChronoUnit.MILLIS));
        }  
    } 
  }
  
  private <T extends BaseEvent> KafkaService.KafkaSendResult send(T paramT, Producer<String, String> paramProducer, ProducerRecord<String, String> paramProducerRecord) {
    ZonedDateTime zonedDateTime2;
    KafkaService.KafkaSendResult kafkaSendResult = new KafkaService.KafkaSendResult();
    Timer timer1 = this.metricRegistry.timer("kafka.[*].requests");
    Timer timer2 = this.metricRegistry.timer("kafka.[" + paramProducerRecord.topic() + "].requests");
    Meter meter1 = this.metricRegistry.meter("kafka.[*].failures");
    Meter meter2 = this.metricRegistry.meter("kafka.[" + paramProducerRecord.topic() + "].failures");
    ZonedDateTime zonedDateTime1 = null;
    try {
      Timer.Context context = timer1.time();
      try {
        Timer.Context context1 = timer2.time();
        try {
          zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC);
          paramProducer.send(paramProducerRecord).get();
          zonedDateTime2 = ZonedDateTime.now(ZoneOffset.UTC);
          kafkaSendResult.success = true;
          if (context1 != null)
            context1.close(); 
        } catch (Throwable throwable) {
          if (context1 != null)
            try {
              context1.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
        if (context != null)
          context.close(); 
      } catch (Throwable throwable) {
        if (context != null)
          try {
            context.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (ExecutionException|InterruptedException|org.apache.kafka.common.KafkaException executionException) {
      meter1.mark();
      meter2.mark();
      zonedDateTime2 = ZonedDateTime.now(ZoneOffset.UTC);
      kafkaSendResult.exception = executionException;
      kafkaSendResult.message = "Failed to send a message to topic [" + paramProducerRecord.topic() + "]\nMessage:\n" + (String)paramProducerRecord.value();
      EventLogType eventLogType = (executionException instanceof org.apache.kafka.common.KafkaException) ? EventLogType.Debug : EventLogType.Error;
      boolean bool = !(paramT instanceof io.fusionauth.domain.event.EventLogCreateEvent) ? true : false;
      EventLogHelper.create(new KafkaService.KafkaErrorEventLog(eventLogType, kafkaSendResult.message, executionException), bool);
    } 
    if (paramT != null) {
      WebhookCallResponse webhookCallResponse = (new WebhookCallResponse()).with(paramWebhookCallResponse -> paramWebhookCallResponse.exception = (paramKafkaSendResult.exception != null) ? ToString.toString(paramKafkaSendResult.exception) : null).with(paramWebhookCallResponse -> paramWebhookCallResponse.statusCode = paramKafkaSendResult.success ? 200 : 500);
      ZonedDateTime zonedDateTime3 = zonedDateTime1;
      ZonedDateTime zonedDateTime4 = zonedDateTime2;
      this.webhookEventLogService.createWebhookAttemptLog(() -> (new WebhookAttemptLog()).with(()).with(()).with(()).with(()).with(()));
    } 
    return kafkaSendResult;
  }
  
  private <T extends BaseEvent> KafkaService.KafkaSendResult sendMessage(T paramT, ProducerRecord<String, String> paramProducerRecord) {
    KafkaService.KafkaSendResult kafkaSendResult = new KafkaService.KafkaSendResult();
    if (!(getConfiguration()).enabled) {
      logger.error("Unable to send message to topic [" + paramProducerRecord.topic() + "]. Kafka integration has been disabled.");
      return kafkaSendResult;
    } 
    return send(paramT, this.kafkaProducerProvider.get(), paramProducerRecord);
  }
}
