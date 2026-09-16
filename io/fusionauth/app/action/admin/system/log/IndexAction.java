package io.fusionauth.app.action.admin.system.log;

import com.google.inject.Inject;
import io.fusionauth.api.domain.FusionAuthNodeMapper;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class IndexAction extends BaseAction {
  private final NodeService nodeService;
  
  public List<UUID> nodeIds = new ArrayList<>();
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport, NodeService paramNodeService) {
    super(paramFrontEndSupport);
    this.nodeService = paramNodeService;
  }
  
  public String get() throws IOException {
    this.nodeService.retrieveAll().forEach(paramFusionAuthNode -> this.nodeIds.add(paramFusionAuthNode.id));
    return "input";
  }
}
