package io.fusionauth.app.freemarker;

import com.google.inject.Inject;
import freemarker.template.Configuration;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.http.server.HTTPContext;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import java.util.Deque;
import java.util.Set;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.control.guice.ControlFactory;
import org.primeframework.mvc.freemarker.FreeMarkerMap;
import org.primeframework.mvc.freemarker.guice.TemplateModelFactory;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class FusionAuthFreeMarkerMap extends FreeMarkerMap {
  @Inject
  public FusionAuthFreeMarkerMap(HTTPContext paramHTTPContext, HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse, ExpressionEvaluator paramExpressionEvaluator, ActionInvocationStore paramActionInvocationStore, MessageStore paramMessageStore, ControlFactory paramControlFactory, TemplateModelFactory paramTemplateModelFactory, Configuration paramConfiguration) {
    super(paramHTTPContext, paramHTTPRequest, paramHTTPResponse, paramExpressionEvaluator, paramActionInvocationStore, paramMessageStore, paramControlFactory, paramTemplateModelFactory, paramConfiguration);
  }
  
  public TemplateModel get(String paramString) {
    TemplateModel templateModel = super.get(paramString);
    if (templateModel == null)
      return null; 
    if (paramString.equals("fusionAuth") && 
      this.objects.containsKey(paramString)) {
      Deque deque = this.actionInvocationStore.getDeque();
      if (deque != null) {
        boolean bool = deque.stream().anyMatch(paramActionInvocation -> paramActionInvocation.action instanceof io.fusionauth.app.action.admin.theme.PreviewAction);
        if (!bool)
          for (ActionInvocation actionInvocation : deque) {
            if (actionInvocation.action != null && 
              BaseAction.class.isAssignableFrom(actionInvocation.action.getClass()))
              return templateModel; 
          }  
      } 
      return (TemplateModel)new FilteringTemplateHashModelEx((TemplateHashModelEx)templateModel, Set.of("statics"));
    } 
    return templateModel;
  }
}
