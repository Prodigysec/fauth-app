package io.fusionauth.api.domain.ip.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import java.util.ArrayList;
import java.util.List;

public final class CityResponse {
  public final City city;
  
  public final Continent continent;
  
  public final Country country;
  
  public final Location location;
  
  public final Postal postal;
  
  public final Country registeredCountry;
  
  public final RepresentedCountry representedCountry;
  
  public final List<Subdivision> subdivisions;
  
  public final Traits traits;
  
  @MaxMindDbConstructor
  public CityResponse(@MaxMindDbParameter(name = "city") City paramCity, @MaxMindDbParameter(name = "continent") Continent paramContinent, @MaxMindDbParameter(name = "country") Country paramCountry1, @MaxMindDbParameter(name = "location") Location paramLocation, @MaxMindDbParameter(name = "postal") Postal paramPostal, @MaxMindDbParameter(name = "registered_country") Country paramCountry2, @MaxMindDbParameter(name = "represented_country") RepresentedCountry paramRepresentedCountry, @MaxMindDbParameter(name = "subdivisions") ArrayList<Subdivision> paramArrayList, @MaxMindDbParameter(name = "traits") Traits paramTraits) {
    this.city = (paramCity != null) ? paramCity : new City();
    this.continent = (paramContinent != null) ? paramContinent : new Continent();
    this.country = (paramCountry1 != null) ? paramCountry1 : new Country();
    this.location = (paramLocation != null) ? paramLocation : new Location();
    this.registeredCountry = (paramCountry2 != null) ? paramCountry2 : new Country();
    this.representedCountry = (paramRepresentedCountry != null) ? paramRepresentedCountry : new RepresentedCountry();
    this.postal = (paramPostal != null) ? paramPostal : new Postal();
    this.subdivisions = (paramArrayList != null) ? paramArrayList : List.of();
    this.traits = (paramTraits != null) ? paramTraits : new Traits();
  }
}
