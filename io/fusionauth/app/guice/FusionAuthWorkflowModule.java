package io.fusionauth.app.guice;

import com.google.inject.Singleton;
import org.primeframework.mvc.action.result.MVCWorkflowFinalizer;
import org.primeframework.mvc.workflow.guice.WorkflowModule;

public class FusionAuthWorkflowModule extends WorkflowModule {
  protected void bindMVCWorkflowFinalizer() {
    bind(MVCWorkflowFinalizer.class).to(FusionAuthMVCWorkflowFinalizer.class).in(Singleton.class);
  }
}
