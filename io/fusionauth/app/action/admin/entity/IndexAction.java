package io.fusionauth.app.action.admin.entity;

import com.google.inject.Inject;
import io.fusionauth.api.service.lock.ReindexDistributedLock;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "entity_manager"})
public class IndexAction extends BaseAction {
  private final ReindexDistributedLock reindexDistributedLock;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport, ReindexDistributedLock paramReindexDistributedLock) {
    super(paramFrontEndSupport);
    this.reindexDistributedLock = paramReindexDistributedLock;
  }
  
  public String get() {
    if (this.reindexDistributedLock.isLocked())
      this.frontEndSupport.addGeneralInfo("reindex-in-progress", new Object[0]); 
    return "input";
  }
}
