package io.fusionauth.app.guice;

import com.google.inject.Inject;
import io.fusionauth.api.domain.api.RequestContext;
import org.primeframework.mvc.action.result.DefaultMVCWorkflowFinalizer;
import org.primeframework.mvc.action.result.ResultStore;

public class FusionAuthMVCWorkflowFinalizer extends DefaultMVCWorkflowFinalizer {
  @Inject
  public FusionAuthMVCWorkflowFinalizer(ResultStore paramResultStore) {
    super(paramResultStore);
  }
  
  public void run() {
    super.run();
    RequestContext.clear();
  }
}
