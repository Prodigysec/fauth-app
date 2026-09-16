package io.fusionauth.app.action.ajax.report;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.DisplayableRawLogin;
import io.fusionauth.domain.api.user.RecentLoginResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true)
public class LoginLocationsAction extends BaseAJAXAction {
  public int limit = 100;
  
  public int offset = 0;
  
  @JSONResponse
  public GeoJSON response = new GeoJSON();
  
  @Inject
  public LoginLocationsAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    List<DisplayableRawLogin> list = ((RecentLoginResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveRecentLogins(this.offset, Integer.valueOf(this.limit)))).logins;
    this.response

      
      .features = (List<Feature>)list.stream().filter(paramDisplayableRawLogin -> (paramDisplayableRawLogin.location != null && paramDisplayableRawLogin.location.latitude != 0.0D && paramDisplayableRawLogin.location.longitude != 0.0D)).map(Feature::new).collect(Collectors.toList());
    return "render-json";
  }
  
  public static class Feature {
    public final String type = "Feature";
    
    public LoginLocationsAction.Geometry geometry;
    
    public Feature(DisplayableRawLogin param1DisplayableRawLogin) {
      this.geometry = new LoginLocationsAction.Geometry(param1DisplayableRawLogin);
    }
  }
  
  public static class GeoJSON {
    public List<LoginLocationsAction.Feature> features = new ArrayList<>();
    
    public String type = "FeatureCollection";
  }
  
  public static class Geometry {
    public final String type = "Point";
    
    public double[] coordinates;
    
    public Geometry(DisplayableRawLogin param1DisplayableRawLogin) {
      this.coordinates = new double[] { param1DisplayableRawLogin.location.longitude, param1DisplayableRawLogin.location.latitude };
    }
  }
}
