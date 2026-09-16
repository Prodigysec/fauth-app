package io.fusionauth.api.domain;

import java.util.List;

public final class Roles {
  public static final String acl_deleter = "acl_deleter";
  
  public static final String acl_manager = "acl_manager";
  
  public static final String admin = "admin";
  
  public static final String api_key_manager = "api_key_manager";
  
  public static final String application_deleter = "application_deleter";
  
  public static final String application_manager = "application_manager";
  
  public static final String audit_log_viewer = "audit_log_viewer";
  
  public static final String connector_deleter = "connector_deleter";
  
  public static final String connector_manager = "connector_manager";
  
  public static final String consent_deleter = "consent_deleter";
  
  public static final String consent_manager = "consent_manager";
  
  public static final String email_template_manager = "email_template_manager";
  
  public static final String entity_manager = "entity_manager";
  
  public static final String event_log_viewer = "event_log_viewer";
  
  public static final String form_deleter = "form_deleter";
  
  public static final String form_manager = "form_manager";
  
  public static final String group_deleter = "group_deleter";
  
  public static final String group_manager = "group_manager";
  
  public static final String key_manager = "key_manager";
  
  public static final String lambda_manager = "lambda_manager";
  
  public static final String message_template_deleter = "message_template_deleter";
  
  public static final String message_template_manager = "message_template_manager";
  
  public static final String messenger_deleter = "messenger_deleter";
  
  public static final String messenger_manager = "messenger_manager";
  
  public static final String mfa_deleter = "mfa_deleter";
  
  public static final String reactor_manager = "reactor_manager";
  
  public static final String report_viewer = "report_viewer";
  
  public static final String system_manager = "system_manager";
  
  public static final String tenant_deleter = "tenant_deleter";
  
  public static final String tenant_manager = "tenant_manager";
  
  public static final String theme_manager = "theme_manager";
  
  public static final String user_action_deleter = "user_action_deleter";
  
  public static final String user_action_manager = "user_action_manager";
  
  public static final String user_deleter = "user_deleter";
  
  public static final String user_manager = "user_manager";
  
  public static final String user_support_manager = "user_support_manager";
  
  public static final String user_support_viewer = "user_support_viewer";
  
  public static final String webhook_event_log_viewer = "webhook_event_log_viewer";
  
  public static final String webhook_manager = "webhook_manager";
  
  public static List<String> all = List.of((Object[])new String[] { 
        "acl_deleter", "acl_manager", "admin", "api_key_manager", "application_deleter", "application_manager", "audit_log_viewer", "connector_deleter", "connector_manager", "consent_deleter", 
        "consent_manager", "email_template_manager", "entity_manager", "event_log_viewer", "form_deleter", "form_manager", "group_deleter", "group_manager", "key_manager", "lambda_manager", 
        "message_template_deleter", "message_template_manager", "messenger_deleter", "messenger_manager", "mfa_deleter", "reactor_manager", "report_viewer", "system_manager", "tenant_deleter", "tenant_manager", 
        "theme_manager", "user_action_deleter", "user_action_manager", "user_deleter", "user_manager", "user_support_manager", "user_support_viewer", "webhook_event_log_viewer", "webhook_manager" });
}
