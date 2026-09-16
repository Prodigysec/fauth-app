package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LogHistory {
  public final List<HistoryItem> historyItems = new ArrayList<>();
  
  public LogHistory() {}
  
  public LogHistory(UUID paramUUID, String paramString, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2) {
    add(paramUUID, paramString, paramZonedDateTime1, paramZonedDateTime2);
  }
  
  public LogHistory add(UUID paramUUID, String paramString, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2) {
    this.historyItems.add(new HistoryItem(paramUUID, paramString, paramZonedDateTime1, paramZonedDateTime2));
    return this;
  }
  
  public HistoryItem earliest() {
    if (this.historyItems.isEmpty())
      return null; 
    return this.historyItems.get(0);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    LogHistory logHistory = (LogHistory)paramObject;
    return this.historyItems.equals(logHistory.historyItems);
  }
  
  public int hashCode() {
    return this.historyItems.hashCode();
  }
  
  public HistoryItem latest() {
    if (this.historyItems.isEmpty())
      return null; 
    return this.historyItems.get(this.historyItems.size() - 1);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public static class HistoryItem {
    public UUID actionerUserId;
    
    public String comment;
    
    public ZonedDateTime createInstant;
    
    public ZonedDateTime expiry;
    
    @JacksonConstructor
    public HistoryItem() {}
    
    public HistoryItem(UUID param1UUID, String param1String, ZonedDateTime param1ZonedDateTime1, ZonedDateTime param1ZonedDateTime2) {
      this.actionerUserId = param1UUID;
      this.comment = param1String;
      this.createInstant = param1ZonedDateTime1;
      this.expiry = param1ZonedDateTime2;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof HistoryItem))
        return false; 
      HistoryItem historyItem = (HistoryItem)param1Object;
      return (Objects.equals(this.actionerUserId, historyItem.actionerUserId) && 
        Objects.equals(this.comment, historyItem.comment) && 
        Objects.equals(this.createInstant, historyItem.createInstant) && 
        Objects.equals(this.expiry, historyItem.expiry));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.actionerUserId, this.comment, this.createInstant, this.expiry });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
}
