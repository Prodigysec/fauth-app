package io.fusionauth.api.service.messaging;

import com.google.inject.Provider;
import com.inversoft.cache.Cache;
import com.inversoft.cache.CacheListener;
import io.fusionauth.domain.Integration;
import io.fusionauth.domain.KafkaConfiguration;
import java.io.Closeable;
import java.time.Duration;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.errors.InterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerProvider implements Provider<Producer<String, String>>, CacheListener<String, Integration>, Closeable {
  private static final Logger logger = LoggerFactory.getLogger(KafkaProducerProvider.class);
  
  private volatile Producer<String, String> producer;
  
  public void close() {
    _close(this.producer);
  }
  
  public Producer<String, String> get() {
    return this.producer;
  }
  
  public void onCacheLoad(Cache<String, Integration> paramCache) {
    Producer<String, String> producer = this.producer;
    KafkaConfiguration kafkaConfiguration = (KafkaConfiguration)paramCache.get("kafka");
    if (kafkaConfiguration.enabled) {
      logger.debug("Rebuild Kafka Producer");
      this.producer = (Producer<String, String>)(new KafkaProducerBuilder(kafkaConfiguration.producer)).build();
    } else {
      this.producer = null;
    } 
    _close(producer);
  }
  
  private void _close(Producer<String, String> paramProducer) {
    if (paramProducer == null)
      return; 
    logger.debug("Shutting down Kafka Producer");
    try {
      paramProducer.close(Duration.ofMillis(500L));
    } catch (InterruptException interruptException) {
      logger.error("Failed to close Kafka Producer.", (Throwable)interruptException);
    } 
  }
}
