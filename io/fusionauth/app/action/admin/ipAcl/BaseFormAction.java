package io.fusionauth.app.action.admin.ipAcl;

import com.inversoft.util.StringTools;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.IPAccessControlEntry;
import io.fusionauth.domain.IPAccessControlEntryAction;
import io.fusionauth.domain.IPAccessControlList;
import java.util.UUID;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

public abstract class BaseFormAction extends BaseAction {
  @FTLVariable
  public static final IPAccessControlEntryAction[] aclActions = IPAccessControlEntryAction.values();
  
  public IPAccessControlList ipAccessControlList = new IPAccessControlList();
  
  public UUID ipAccessControlListId;
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @PostParameterMethod
  public void postParameter() {
    this.ipAccessControlList.entries.removeIf(paramIPAccessControlEntry -> (paramIPAccessControlEntry == null || (StringTools.isTrimmedEmpty(paramIPAccessControlEntry.endIPAddress) && StringTools.isTrimmedEmpty(paramIPAccessControlEntry.startIPAddress))));
  }
}
