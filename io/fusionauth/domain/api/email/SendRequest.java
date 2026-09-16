package io.fusionauth.domain.api.email;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.email.EmailAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class SendRequest {
  public UUID applicationId;
  
  public List<String> bccAddresses;
  
  public List<String> ccAddresses;
  
  public List<Locale> preferredLanguages = new ArrayList<>();
  
  public Map<String, Object> requestData;
  
  public List<EmailAddress> toAddresses;
  
  public List<UUID> userIds;
  
  @JacksonConstructor
  public SendRequest() {}
  
  public SendRequest(List<UUID> paramList, List<String> paramList1, List<String> paramList2, Map<String, Object> paramMap) {
    this.userIds = paramList;
    this.ccAddresses = paramList1;
    this.bccAddresses = paramList2;
    this.requestData = paramMap;
  }
  
  public SendRequest(List<UUID> paramList, Map<String, Object> paramMap) {
    this.userIds = paramList;
    this.requestData = paramMap;
  }
  
  public SendRequest(List<UUID> paramList) {
    this.userIds = paramList;
  }
  
  public SendRequest normalize() {
    this.requestData = Optional.<Map<String, Object>>ofNullable(this.requestData).orElseGet(java.util.HashMap::new);
    this.userIds = Optional.<List<UUID>>ofNullable(this.userIds).orElseGet(ArrayList::new);
    this.toAddresses = Optional.<List<EmailAddress>>ofNullable(this.toAddresses).orElseGet(ArrayList::new);
    this.ccAddresses = Optional.<List<String>>ofNullable(this.ccAddresses).orElseGet(ArrayList::new);
    this.bccAddresses = Optional.<List<String>>ofNullable(this.bccAddresses).orElseGet(ArrayList::new);
    return this;
  }
}
