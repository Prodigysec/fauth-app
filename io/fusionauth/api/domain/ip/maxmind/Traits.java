package io.fusionauth.api.domain.ip.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import com.maxmind.db.Network;

public final class Traits {
  public final Long autonomousSystemNumber;
  
  public final String autonomousSystemOrganization;
  
  public final ConnectionType connectionType;
  
  public final String domain;
  
  public final String ipAddress;
  
  public final Boolean isAnonymous;
  
  public final Boolean isAnonymousProxy;
  
  public final Boolean isAnonymousVpn;
  
  public final Boolean isHostingProvider;
  
  public final Boolean isLegitimateProxy;
  
  public final Boolean isPublicProxy;
  
  public final Boolean isResidentialProxy;
  
  public final Boolean isSatelliteProvider;
  
  public final Boolean isTorExitNode;
  
  public final String isp;
  
  public final Network network;
  
  public final String organization;
  
  public final Double staticIpScore;
  
  public final Integer userCount;
  
  public final String userType;
  
  public Traits() {
    this.autonomousSystemNumber = null;
    this.autonomousSystemOrganization = null;
    this.connectionType = null;
    this.domain = null;
    this.ipAddress = null;
    this.isAnonymous = Boolean.valueOf(false);
    this.isAnonymousProxy = Boolean.valueOf(false);
    this.isAnonymousVpn = Boolean.valueOf(false);
    this.isHostingProvider = Boolean.valueOf(false);
    this.isLegitimateProxy = Boolean.valueOf(false);
    this.isp = null;
    this.isPublicProxy = Boolean.valueOf(false);
    this.isResidentialProxy = Boolean.valueOf(false);
    this.isSatelliteProvider = Boolean.valueOf(false);
    this.isTorExitNode = Boolean.valueOf(false);
    this.network = null;
    this.organization = null;
    this.staticIpScore = null;
    this.userCount = null;
    this.userType = null;
  }
  
  @MaxMindDbConstructor
  public Traits(@MaxMindDbParameter(name = "autonomous_system_number") Long paramLong, @MaxMindDbParameter(name = "autonomous_system_organization") String paramString1, @MaxMindDbParameter(name = "connection_type") String paramString2, @MaxMindDbParameter(name = "domain") String paramString3, @MaxMindDbParameter(name = "ip_address") String paramString4, @MaxMindDbParameter(name = "is_anonymous") Boolean paramBoolean1, @MaxMindDbParameter(name = "is_anonymous_proxy") Boolean paramBoolean2, @MaxMindDbParameter(name = "is_anonymous_vpn") Boolean paramBoolean3, @MaxMindDbParameter(name = "is_hosting_provider") Boolean paramBoolean4, @MaxMindDbParameter(name = "is_legitimate_proxy") Boolean paramBoolean5, @MaxMindDbParameter(name = "is_public_proxy") Boolean paramBoolean6, @MaxMindDbParameter(name = "is_residential_proxy") Boolean paramBoolean7, @MaxMindDbParameter(name = "is_satellite_provider") Boolean paramBoolean8, @MaxMindDbParameter(name = "is_tor_exit_node") Boolean paramBoolean9, @MaxMindDbParameter(name = "isp") String paramString5, @MaxMindDbParameter(name = "network") Network paramNetwork, @MaxMindDbParameter(name = "organization") String paramString6, @MaxMindDbParameter(name = "user_type") String paramString7, @MaxMindDbParameter(name = "user_count") Integer paramInteger, @MaxMindDbParameter(name = "static_ip_score") Double paramDouble) {
    this.autonomousSystemNumber = paramLong;
    this.autonomousSystemOrganization = paramString1;
    this.connectionType = ConnectionType.fromString(paramString2);
    this.domain = paramString3;
    this.ipAddress = paramString4;
    this.isAnonymous = paramBoolean1;
    this.isAnonymousProxy = paramBoolean2;
    this.isAnonymousVpn = paramBoolean3;
    this.isHostingProvider = paramBoolean4;
    this.isLegitimateProxy = paramBoolean5;
    this.isPublicProxy = paramBoolean6;
    this.isResidentialProxy = paramBoolean7;
    this.isSatelliteProvider = paramBoolean8;
    this.isTorExitNode = paramBoolean9;
    this.isp = paramString5;
    this.network = paramNetwork;
    this.organization = paramString6;
    this.userType = paramString7;
    this.userCount = paramInteger;
    this.staticIpScore = paramDouble;
  }
}
