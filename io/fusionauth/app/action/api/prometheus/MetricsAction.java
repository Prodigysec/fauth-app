package io.fusionauth.app.action.api.prometheus;

import com.google.inject.Inject;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Stream;

@Stream
@Action(requiresAuthentication = true, scheme = {"api-no-tenant", "api-basic-auth", "user", "local-metrics"})
public final class MetricsAction extends BaseAPIAction {
  public final String name;
  
  public final String type;
  
  private final CollectorRegistry registry;
  
  public int length;
  
  public Set<String> names;
  
  public InputStream stream;
  
  @Inject
  public MetricsAction(CollectorRegistry paramCollectorRegistry, FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    this.registry = paramCollectorRegistry;
    this.type = "text/plain; version=0.0.4; charset=utf-8";
    this.name = "";
    this.names = Collections.emptySet();
  }
  
  public String get() {
    try {
      StringWriter stringWriter = new StringWriter();
      try {
        TextFormat.write004(stringWriter, this.registry.filteredMetricFamilySamples(this.names));
        this.stream = new ByteArrayInputStream(stringWriter.toString().getBytes());
        this.length = stringWriter.getBuffer().length();
        stringWriter.close();
      } catch (Throwable throwable) {
        try {
          stringWriter.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (IOException iOException) {
      throw new ErrorException(iOException, new Object[0]);
    } 
    return "success";
  }
}
