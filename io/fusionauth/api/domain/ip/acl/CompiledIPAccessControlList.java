package io.fusionauth.api.domain.ip.acl;

import com.inversoft.validator.IPValidator;
import io.fusionauth.api.util.NetworkTools;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.IPAccessControlEntry;
import io.fusionauth.domain.IPAccessControlEntryAction;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CompiledIPAccessControlList implements Buildable<CompiledIPAccessControlList> {
  private final SortedSet<CompiledIPAccessControlEntry> sorted = new TreeSet<>();
  
  public IPAccessControlEntryAction defaultAction;
  
  public CompiledIPAccessControlList(List<IPAccessControlEntry> paramList) {
    for (IPAccessControlEntry iPAccessControlEntry : paramList) {
      if (iPAccessControlEntry.startIPAddress.equals("*")) {
        this.defaultAction = iPAccessControlEntry.action;
        continue;
      } 
      this.sorted.add(new CompiledIPAccessControlEntry(iPAccessControlEntry));
    } 
  }
  
  public boolean isBlocked(String paramString) {
    CompiledIPAccessControlEntry compiledIPAccessControlEntry = null;
    if (IPValidator.isValidIPv4(paramString)) {
      long l = NetworkTools.convertIPv4ToLong(paramString);
      for (CompiledIPAccessControlEntry compiledIPAccessControlEntry1 : this.sorted) {
        if (l < compiledIPAccessControlEntry1.start)
          break; 
        if (l <= compiledIPAccessControlEntry1.end)
          compiledIPAccessControlEntry = compiledIPAccessControlEntry1; 
      } 
    } 
    return (compiledIPAccessControlEntry == null) ? (
      (this.defaultAction == IPAccessControlEntryAction.Block)) : (
      (compiledIPAccessControlEntry.action == IPAccessControlEntryAction.Block));
  }
}
