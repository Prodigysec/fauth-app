package io.fusionauth.api.service.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLoggingService implements LoggingService {
  public LoggingService.LoggingLevel getLevel(String paramString) {
    if (paramString == null)
      paramString = ""; 
    Logger logger = LoggerFactory.getLogger(paramString);
    return logbackLevelToLoggingLevel(((Logger)logger).getEffectiveLevel());
  }
  
  public void setLevel(String paramString, LoggingService.LoggingLevel paramLoggingLevel) {
    if (paramString == null)
      paramString = ""; 
    Logger logger = LoggerFactory.getLogger(paramString);
    ((Logger)logger).setLevel(loggingLevelToLogback(paramLoggingLevel));
  }
  
  private LoggingService.LoggingLevel logbackLevelToLoggingLevel(Level paramLevel) {
    switch (paramLevel.toInt()) {
      case 2147483647:
        return LoggingService.LoggingLevel.off;
      case 40000:
        return LoggingService.LoggingLevel.error;
      case 30000:
        return LoggingService.LoggingLevel.warn;
      case 20000:
        return LoggingService.LoggingLevel.info;
      case 10000:
        return LoggingService.LoggingLevel.debug;
      case 5000:
        return LoggingService.LoggingLevel.trace;
    } 
    return LoggingService.LoggingLevel.trace;
  }
  
  private Level loggingLevelToLogback(LoggingService.LoggingLevel paramLoggingLevel) {
    switch (paramLoggingLevel) {
      case error:
        return Level.ERROR;
      case warn:
        return Level.WARN;
      case info:
        return Level.INFO;
      case debug:
        return Level.DEBUG;
      case trace:
        return Level.TRACE;
      case off:
        return Level.OFF;
    } 
    return Level.TRACE;
  }
}
