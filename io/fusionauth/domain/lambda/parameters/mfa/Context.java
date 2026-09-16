package io.fusionauth.domain.lambda.parameters.mfa;

import io.fusionauth.domain.Application;
import io.fusionauth.domain.AuthenticationThreats;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.MultiFactorAction;
import java.util.Set;

public class Context {
  public final String accessToken;
  
  public final MultiFactorAction action;
  
  public final Application application;
  
  public final Set<AuthenticationThreats> authenticationThreats;
  
  public final String authenticationType;
  
  public final ClientRisk clientRisk;
  
  public final EventInfo eventInfo;
  
  public final Trust mfaTrust;
  
  public final Policies policies;
  
  public Context(EventInfo paramEventInfo, Set<AuthenticationThreats> paramSet, Trust paramTrust, String paramString1, Policies paramPolicies, MultiFactorAction paramMultiFactorAction, Application paramApplication, String paramString2, ClientRisk paramClientRisk) {
    this.eventInfo = paramEventInfo;
    this.authenticationThreats = paramSet;
    this.mfaTrust = paramTrust;
    this.accessToken = paramString1;
    this.policies = paramPolicies;
    this.action = paramMultiFactorAction;
    this.application = paramApplication;
    this.authenticationType = paramString2;
    this.clientRisk = paramClientRisk;
  }
}
