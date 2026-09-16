package io.fusionauth.app.action.ajax.system.log;

import com.google.inject.Inject;
import com.inversoft.util.Pair;
import io.fusionauth.api.domain.FusionAuthNodeMapper;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.system.SystemLogFrontendService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, value = "{id}", constraints = {"admin", "event_log_viewer"})
public class IndexAction extends BaseAJAXAction {
  private final SystemLogFrontendService systemLogFrontendService;
  
  public int lastNBytes = 65536;
  
  public List<Pair<String, String>> logs;
  
  public FusionAuthNodeMapper.FusionAuthNode node;
  
  public int nodeCount;
  
  public UUID nodeId;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport, SystemLogFrontendService paramSystemLogFrontendService) {
    super(paramFrontEndSupport);
    this.systemLogFrontendService = paramSystemLogFrontendService;
  }
  
  public String get() throws IOException {
    SystemLogFrontendService.SystemLogResult systemLogResult = this.systemLogFrontendService.retrieveLogsByNodeId(this.nodeId, this.lastNBytes);
    this.logs = systemLogResult.logs;
    this.nodeCount = systemLogResult.nodeCount;
    this.node = systemLogResult.node;
    return "render";
  }
}
