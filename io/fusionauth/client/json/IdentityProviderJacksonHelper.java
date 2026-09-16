package io.fusionauth.client.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.fusionauth.domain.provider.AppleIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.EpicGamesIdentityProvider;
import io.fusionauth.domain.provider.ExternalJWTIdentityProvider;
import io.fusionauth.domain.provider.FacebookIdentityProvider;
import io.fusionauth.domain.provider.GoogleIdentityProvider;
import io.fusionauth.domain.provider.HYPRIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.LinkedInIdentityProvider;
import io.fusionauth.domain.provider.NintendoIdentityProvider;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdPInitiatedIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import io.fusionauth.domain.provider.SonyPSNIdentityProvider;
import io.fusionauth.domain.provider.SteamIdentityProvider;
import io.fusionauth.domain.provider.TwitchIdentityProvider;
import io.fusionauth.domain.provider.TwitterIdentityProvider;
import io.fusionauth.domain.provider.XboxIdentityProvider;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class IdentityProviderJacksonHelper {
  public static IdentityProviderType extractType(DeserializationContext paramDeserializationContext, JsonParser paramJsonParser, JsonNode paramJsonNode) throws IOException {
    JsonNode jsonNode = paramJsonNode.at("/type");
    String str = jsonNode.isMissingNode() ? IdentityProviderType.ExternalJWT.name() : jsonNode.asText(IdentityProviderType.ExternalJWT.name());
    IdentityProviderType identityProviderType = IdentityProviderType.safeValueOf(str);
    if (identityProviderType == null) {
      String str1 = Arrays.<IdentityProviderType>stream(IdentityProviderType.values()).map(Enum::name).sorted().collect(Collectors.joining(", "));
      return (IdentityProviderType)paramDeserializationContext.handleUnexpectedToken(BaseIdentityProvider.class, jsonNode.asToken(), paramJsonParser, "Expected the type field to be one of [" + str1 + "], but found [" + jsonNode
          .asText() + "]", new Object[0]);
    } 
    return identityProviderType;
  }
  
  public static BaseIdentityProvider<?> newIdentityProvider(IdentityProviderType paramIdentityProviderType) {
    switch (paramIdentityProviderType) {
      case Apple:
        return new AppleIdentityProvider();
      case EpicGames:
        return new EpicGamesIdentityProvider();
      case ExternalJWT:
        return new ExternalJWTIdentityProvider();
      case Facebook:
        return new FacebookIdentityProvider();
      case Google:
        return new GoogleIdentityProvider();
      case HYPR:
        return new HYPRIdentityProvider();
      case LinkedIn:
        return new LinkedInIdentityProvider();
      case Nintendo:
        return new NintendoIdentityProvider();
      case OpenIDConnect:
        return new OpenIdConnectIdentityProvider();
      case SAMLv2:
        return new SAMLv2IdentityProvider();
      case SAMLv2IdPInitiated:
        return new SAMLv2IdPInitiatedIdentityProvider();
      case SonyPSN:
        return new SonyPSNIdentityProvider();
      case Steam:
        return new SteamIdentityProvider();
      case Twitch:
        return new TwitchIdentityProvider();
      case Twitter:
        return new TwitterIdentityProvider();
      case Xbox:
        return new XboxIdentityProvider();
    } 
    throw new IllegalArgumentException();
  }
}
