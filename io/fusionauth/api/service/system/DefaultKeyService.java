package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.error.Errors;
import com.inversoft.util.SecurityTools;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.ConnectorConfigurationMapper;
import io.fusionauth.api.domain.EntityMapper;
import io.fusionauth.api.domain.IdentityProviderMapper;
import io.fusionauth.api.domain.KeyMapper;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.domain.WebhookMapper;
import io.fusionauth.api.security.SecurityTools;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.GenericConnectorConfiguration;
import io.fusionauth.domain.provider.AppleApplicationConfiguration;
import io.fusionauth.domain.provider.AppleIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.SAMLv2IdPInitiatedIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import io.fusionauth.domain.util.Normalizer;
import io.fusionauth.jwks.JSONWebKeyBuilder;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.pem.domain.PEM;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultKeyService implements KeyService {
  private static final Set<Key.KeyAlgorithm> SUPPORTED_ALGORITHMS = Set.of(Key.KeyAlgorithm.ES256, Key.KeyAlgorithm.ES384, Key.KeyAlgorithm.ES512, Key.KeyAlgorithm.HS256, Key.KeyAlgorithm.HS384, Key.KeyAlgorithm.HS512, Key.KeyAlgorithm.RS256, Key.KeyAlgorithm.RS384, Key.KeyAlgorithm.RS512, Key.KeyAlgorithm.Ed25519);
  
  private final ApplicationMapper applicationMapper;
  
  private final CacheNotifier cacheNotifier;
  
  private final ConnectorConfigurationMapper connectorConfigurationMapper;
  
  private final EntityMapper entityMapper;
  
  private final IdentityProviderMapper identityProviderMapper;
  
  private final KeyMapper keyMapper;
  
  private final KeyReaderService keyReader;
  
  private final TenantMapper tenantMapper;
  
  private final WebhookMapper webhookMapper;
  
  @Inject
  public DefaultKeyService(ApplicationMapper paramApplicationMapper, CacheNotifier paramCacheNotifier, ConnectorConfigurationMapper paramConnectorConfigurationMapper, EntityMapper paramEntityMapper, IdentityProviderMapper paramIdentityProviderMapper, KeyMapper paramKeyMapper, KeyReaderService paramKeyReaderService, TenantMapper paramTenantMapper, WebhookMapper paramWebhookMapper) {
    this.applicationMapper = paramApplicationMapper;
    this.cacheNotifier = paramCacheNotifier;
    this.connectorConfigurationMapper = paramConnectorConfigurationMapper;
    this.entityMapper = paramEntityMapper;
    this.identityProviderMapper = paramIdentityProviderMapper;
    this.keyMapper = paramKeyMapper;
    this.keyReader = paramKeyReaderService;
    this.tenantMapper = paramTenantMapper;
    this.webhookMapper = paramWebhookMapper;
  }
  
  public void create(Key paramKey) {
    if (paramKey.id == null)
      paramKey.id = UUID.randomUUID(); 
    paramKey.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramKey.lastUpdateInstant = paramKey.insertInstant;
    if (paramKey.type == Key.KeyType.Secret) {
      paramKey.algorithm = Key.KeyAlgorithm.None;
      paramKey.kid = paramKey.name;
      this.keyMapper.create(paramKey);
      this.cacheNotifier.reload("Keys");
      return;
    } 
    if (paramKey.algorithm != null && paramKey.privateKey == null && paramKey.publicKey == null && paramKey.secret == null)
      generateKeys(paramKey); 
    if (paramKey.type == null && paramKey.secret != null) {
      paramKey.type = Key.KeyType.HMAC;
    } else if (paramKey.type == null && paramKey.publicKey != null) {
      paramKey.type = SecurityTools.keyTypeFromPEM(paramKey.publicKey);
    } else if (paramKey.type == null && paramKey.certificate != null) {
      paramKey.type = SecurityTools.keyTypeFromPEM(paramKey.certificate);
    } else if (paramKey.privateKey != null) {
      paramKey.type = SecurityTools.keyTypeFromPEM(paramKey.privateKey);
    } 
    if (paramKey.issuer == null && paramKey.type != Key.KeyType.HMAC)
      paramKey.issuer = (this.tenantMapper.retrieveTenantByApplicationId(Application.FUSIONAUTH_APP_ID)).issuer; 
    if (paramKey.certificate != null) {
      PEM pEM = PEM.decode(paramKey.certificate);
      Certificate certificate = pEM.certificate;
      if (certificate instanceof X509Certificate) {
        X509Certificate x509Certificate = (X509Certificate)certificate;
        Date date = x509Certificate.getNotAfter();
        if (date != null)
          paramKey.expirationInstant = ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC); 
      } 
      if (paramKey.publicKey == null)
        paramKey.publicKey = PEM.encode(pEM.publicKey); 
    } 
    KeyHelper.setupSyntheticFields(paramKey);
    if (paramKey.kid == null)
      if (paramKey.certificate == null) {
        paramKey
          
          .kid = (paramKey.publicKey != null) ? JWTUtils.generateJWS_kid_S256((new JSONWebKeyBuilder()).build(paramKey.publicKey)) : SecurityTools.secureRandom(24);
      } else {
        PEM pEM = PEM.decode(paramKey.certificate);
        try {
          paramKey.kid = JWTUtils.generateJWS_x5t(pEM.certificate.getEncoded());
        } catch (CertificateEncodingException certificateEncodingException) {
          paramKey.kid = SecurityTools.secureRandom(24);
        } 
      }  
    this.keyMapper.create(paramKey);
    this.cacheNotifier.reload("Keys");
  }
  
  public void delete(UUID paramUUID) {
    if (this.keyMapper.delete(paramUUID) == 0)
      throw new NotFoundException(); 
    this.cacheNotifier.reload("Keys");
  }
  
  public Key resetDefaultKey() {
    Key key1 = this.keyReader.retrieveByName("Default signing key");
    if (key1 != null)
      update(key1, key1.with(paramKey2 -> paramKey2.name = "[Renamed " + String.valueOf(UUID.randomUUID()) + "] " + paramKey1.name)); 
    Key key2 = (new Key()).with(paramKey -> paramKey.id = UUID.randomUUID()).with(paramKey -> paramKey.algorithm = Key.KeyAlgorithm.HS256).with(paramKey -> paramKey.type = Key.KeyType.HMAC).with(paramKey -> paramKey.name = "Default signing key").with(paramKey2 -> paramKey2.insertInstant = (paramKey1 != null) ? paramKey1.insertInstant : ZonedDateTime.now(ZoneOffset.UTC)).with(paramKey -> paramKey.kid = SecurityTools.secureRandom(24)).with(paramKey -> paramKey.lastUpdateInstant = paramKey.insertInstant).with(paramKey -> paramKey.secret = JWTUtils.generateSHA256_HMACSecret());
    this.keyMapper.create(key2);
    return key2;
  }
  
  public void update(Key paramKey1, Key paramKey2) {
    paramKey2.algorithm = paramKey1.algorithm;
    paramKey2.certificate = paramKey1.certificate;
    paramKey2.certificateInformation = paramKey1.certificateInformation;
    paramKey2.expirationInstant = paramKey1.expirationInstant;
    paramKey2.hasPrivateKey = paramKey1.hasPrivateKey;
    paramKey2.insertInstant = paramKey1.insertInstant;
    paramKey2.issuer = paramKey1.issuer;
    paramKey2.kid = paramKey1.kid;
    paramKey2.length = paramKey1.length;
    paramKey2.privateKey = paramKey1.privateKey;
    paramKey2.publicKey = paramKey1.publicKey;
    paramKey2.type = paramKey1.type;
    if (paramKey1.type == Key.KeyType.Secret) {
      if (paramKey2.secret == null || paramKey2.secret.isBlank())
        paramKey2.secret = paramKey1.secret; 
      paramKey2.kid = paramKey2.name;
    } else {
      paramKey2.secret = paramKey1.secret;
    } 
    paramKey2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.keyMapper.update(paramKey2);
    KeyHelper.setupSyntheticFields(paramKey2);
  }
  
  public KeyService.ValidationResult validate(Key paramKey, boolean paramBoolean) {
    KeyService.ValidationResult validationResult = new KeyService.ValidationResult();
    validationResult.existing = paramBoolean ? null : this.keyReader.retrieveById(paramKey.id);
    validationResult.key = paramKey;
    validationResult

































      
      .errors = (new Validator()).ifTrue(paramBoolean, paramValidator -> paramValidator.notDuplicate((paramKey.id != null) ? this.keyReader.retrieveById(paramKey.id) : null, "keyId", new Object[] { paramKey.id }).notDuplicate((paramKey.name != null) ? this.keyReader.retrieveByName(paramKey.name) : null, "key.name", new Object[] { paramKey.name }).notMissing(paramKey.algorithm, "key.algorithm", new Object[0]).ifLastCheckHadNoError(()).ifTrue(SecurityTools.isRSA(paramKey.algorithm), ())).ifFalse(paramBoolean, paramValidator -> paramValidator.notDuplicate((paramKey.name != null) ? this.keyMapper.retrieveExisting(paramKey.id, paramKey.name) : null, "key.name", new Object[] { paramKey.name }).ensure(!ClientSecretShadowKeys.contains(paramKey.id), "key.name", "[shadow]", new Object[] { paramKey.id }).ifTrue((paramValidationResult.existing != null && paramValidationResult.existing.type == Key.KeyType.Secret), ())).notBlank(paramKey.name, "key.name", new Object[0]).done();
    return validationResult;
  }
  
  public Errors validateDelete(UUID paramUUID) {
    return (new Validator())
      
      .forEach(this.tenantMapper.retrieveAll(), (paramValidator, paramTenant, paramInteger) -> paramValidator.notInUse(!paramTenant.jwtConfiguration.accessTokenKeyId.equals(paramUUID), "keyId", new Object[] { paramUUID, "Tenant [" + paramTenant.name + "] JWT Configuration for Access Token Signing Key" }).notInUse(!paramTenant.jwtConfiguration.idTokenKeyId.equals(paramUUID), "keyId", new Object[] { paramUUID, "Tenant [" + paramTenant.name + "] JWT Configuration for Id Token Signing Key" })).forEach(this.applicationMapper.retrieveAllIgnoreActive(null), (paramValidator, paramApplication, paramInteger) -> paramValidator.ifTrue((paramApplication.jwtConfiguration.accessTokenKeyId != null), ()).ifTrue((paramApplication.jwtConfiguration.idTokenKeyId != null), ()).ifTrue((paramApplication.samlv2Configuration.keyId != null), ()).ifTrue((paramApplication.samlv2Configuration.logout.singleLogout.keyId != null), ()).ifTrue((paramApplication.samlv2Configuration.logout.keyId != null), ()).ifTrue((paramApplication.samlv2Configuration.assertionEncryptionConfiguration.keyTransportEncryptionKeyId != null), ()))



















      
      .forEach(this.identityProviderMapper.retrieveByType(null, IdentityProviderType.Apple), (paramValidator, paramBaseIdentityProvider, paramInteger) -> paramValidator.ifTrue((((AppleIdentityProvider)paramBaseIdentityProvider).keyId != null), ()).forEach(paramBaseIdentityProvider.applicationConfiguration.entrySet(), ()))












      
      .forEach(this.identityProviderMapper.retrieveByType(null, IdentityProviderType.SAMLv2), (paramValidator, paramBaseIdentityProvider, paramInteger) -> paramValidator.ifTrue((((SAMLv2IdentityProvider)paramBaseIdentityProvider).requestSigningKeyId != null), ()).ifTrue((((SAMLv2IdentityProvider)paramBaseIdentityProvider).assertionDecryptionConfiguration.keyTransportDecryptionKeyId != null), ()))






      
      .forEach(this.identityProviderMapper.retrieveByType(null, IdentityProviderType.SAMLv2IdPInitiated), (paramValidator, paramBaseIdentityProvider, paramInteger) -> paramValidator.ifTrue((((SAMLv2IdPInitiatedIdentityProvider)paramBaseIdentityProvider).assertionDecryptionConfiguration.keyTransportDecryptionKeyId != null), ()))




      
      .forEach(this.connectorConfigurationMapper.retrieveAll(), (paramValidator, paramBaseConnectorConfiguration, paramInteger) -> {
          // Byte code:
          //   0: aload_1
          //   1: aload_2
          //   2: instanceof io/fusionauth/domain/connector/GenericConnectorConfiguration
          //   5: ifeq -> 26
          //   8: aload_2
          //   9: checkcast io/fusionauth/domain/connector/GenericConnectorConfiguration
          //   12: astore #4
          //   14: aload #4
          //   16: getfield sslCertificateKeyId : Ljava/util/UUID;
          //   19: ifnull -> 26
          //   22: iconst_1
          //   23: goto -> 27
          //   26: iconst_0
          //   27: aload_1
          //   28: aload_2
          //   29: aload_0
          //   30: <illegal opcode> run : (Lcom/inversoft/validator/Validator;Lio/fusionauth/domain/connector/BaseConnectorConfiguration;Ljava/util/UUID;)Ljava/lang/Runnable;
          //   35: invokevirtual ifTrue : (ZLjava/lang/Runnable;)Lcom/inversoft/validator/Validator;
          //   38: pop
          //   39: return
          // Line number table:
          //   Java source line number -> byte code offset
          //   #348	-> 0
          //   #349	-> 1
          //   #348	-> 39
        }).forEach(this.entityMapper.retrieveAllTypes(), (paramValidator, paramEntityType, paramInteger) -> paramValidator.ifTrue((paramEntityType.jwtConfiguration.accessTokenKeyId != null), ()).ifTrue((paramEntityType.jwtConfiguration.accessTokenVerificationKeyIds != null), ()))







      
      .forEach(this.webhookMapper.retrieveAll(), (paramValidator, paramWebhook, paramInteger) -> paramValidator.ifTrue((paramWebhook.sslCertificateKeyId != null), ()).ifTrue((paramWebhook.signatureConfiguration.signingKeyId != null), ()))







      
      .forEach(this.applicationMapper.retrieveApplicationsUsingVerificationKey(paramUUID), (paramValidator, paramApplicationId, paramInteger) -> paramValidator.notInUse(false, "keyId", new Object[] { paramUUID, "Application [" + paramApplicationId.name + "] with Id [" + String.valueOf(paramApplicationId.id) + "] for signature verification" })).forEach(this.tenantMapper.retrieveTenantsUsingVerificationKey(paramUUID), (paramValidator, paramTenantId, paramInteger) -> paramValidator.notInUse(false, "keyId", new Object[] { paramUUID, "Tenant [" + paramTenantId.name + "] with Id [" + String.valueOf(paramTenantId.id) + "] for signature verification" })).forEach(this.identityProviderMapper.retrieveIdentityProvidersUsingVerificationKey(paramUUID), (paramValidator, paramIdentityProviderId, paramInteger) -> paramValidator.notInUse(false, "keyId", new Object[] { paramUUID, "Identity Provider [" + paramIdentityProviderId.name + "] with Id [" + String.valueOf(paramIdentityProviderId.id) + "] for signature verification" })).ensure(!ClientSecretShadowKeys.contains(paramUUID), "keyId", "[shadow]", new Object[] { paramUUID }).done();
  }
  
  public Errors validateImport(Key paramKey) {
    return (new Validator())
      
      .notDuplicate((paramKey.id != null) ? this.keyReader.retrieveById(paramKey.id) : null, "keyId", new Object[] { paramKey.id }).notDuplicate((paramKey.kid != null) ? this.keyReader.retrieveByKid(paramKey.kid) : null, "key.kid", new Object[] { paramKey.kid }).notDuplicate((paramKey.name != null) ? this.keyReader.retrieveByName(paramKey.name) : null, "key.name", new Object[] { paramKey.name }).notBlank(paramKey.name, "key.name", new Object[0])

      
      .ifTrue((paramKey.type == Key.KeyType.Secret), paramValidator -> paramValidator.notBlank(paramKey.secret, "key.secret", new Object[0]).maxLength(paramKey.secret, 256, "key.secret", new Object[] { Integer.valueOf(256) }).ensure((paramKey.kid == null || paramKey.kid.equals(paramKey.name)), "key.kid", "[invalid]", new Object[0]).notDuplicate((paramKey.name != null) ? this.keyReader.retrieveByKid(paramKey.name) : null, "key.kid", new Object[] { paramKey.name }).ensure((paramKey.publicKey == null), "key.publicKey", "[invalid]", new Object[0]).ensure((paramKey.privateKey == null), "key.privateKey", "[invalid]", new Object[0]).ensure((paramKey.certificate == null), "key.certificate", "[invalid]", new Object[0])).ifTrue((paramKey.type != Key.KeyType.Secret), paramValidator -> paramValidator.ifTrue((paramKey.type == Key.KeyType.HMAC), ()).ifTrue((paramKey.secret != null), ()).ifTrue(
          
          (paramKey.type != null && paramKey.algorithm != null), ()).check(()).ifLastCheckHadNoError(()).ifLastCheckHadNoError(()).ifNoErrors(()).ifTrue((paramKey.privateKey == null), ()).ensure(x5tKidIsUnique(paramKey), "key.kid", "[duplicateX5t]", new Object[0]))






































































































      
      .done();
  }
  
  private boolean certificateIsPEMish(Key paramKey) {
    if (SecurityTools.isPEMEncoded(paramKey.certificate))
      return true; 
    return retryAsCertificatePEM(paramKey);
  }
  
  private String chopIt(String paramString) {
    ArrayList<String> arrayList = new ArrayList();
    String str = Normalizer.removeLineReturns(paramString);
    int i;
    for (i = 0; i < str.length(); ) {
      arrayList.add(str.substring(i, Math.min(i + 64, str.length())));
      i += 64;
    } 
    return String.join("\n", (Iterable)arrayList);
  }
  
  private void generateKeys(Key paramKey) {
    // Byte code:
    //   0: aconst_null
    //   1: astore_2
    //   2: aload_1
    //   3: getfield algorithm : Lio/fusionauth/domain/Key$KeyAlgorithm;
    //   6: invokestatic isHMAC : (Lio/fusionauth/domain/Key$KeyAlgorithm;)Z
    //   9: ifeq -> 108
    //   12: getstatic io/fusionauth/api/service/system/DefaultKeyService$1.$SwitchMap$io$fusionauth$domain$Key$KeyAlgorithm : [I
    //   15: aload_1
    //   16: getfield algorithm : Lio/fusionauth/domain/Key$KeyAlgorithm;
    //   19: invokevirtual ordinal : ()I
    //   22: iaload
    //   23: tableswitch default -> 78, 1 -> 48, 2 -> 58, 3 -> 68
    //   48: aload_1
    //   49: invokestatic generateSHA256_HMACSecret : ()Ljava/lang/String;
    //   52: putfield secret : Ljava/lang/String;
    //   55: goto -> 98
    //   58: aload_1
    //   59: invokestatic generateSHA384_HMACSecret : ()Ljava/lang/String;
    //   62: putfield secret : Ljava/lang/String;
    //   65: goto -> 98
    //   68: aload_1
    //   69: invokestatic generateSHA512_HMACSecret : ()Ljava/lang/String;
    //   72: putfield secret : Ljava/lang/String;
    //   75: goto -> 98
    //   78: new java/lang/IllegalStateException
    //   81: dup
    //   82: aload_1
    //   83: getfield algorithm : Lio/fusionauth/domain/Key$KeyAlgorithm;
    //   86: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   89: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;)Ljava/lang/String;
    //   94: invokespecial <init> : (Ljava/lang/String;)V
    //   97: athrow
    //   98: aload_1
    //   99: getstatic io/fusionauth/domain/Key$KeyType.HMAC : Lio/fusionauth/domain/Key$KeyType;
    //   102: putfield type : Lio/fusionauth/domain/Key$KeyType;
    //   105: goto -> 322
    //   108: aload_1
    //   109: getfield algorithm : Lio/fusionauth/domain/Key$KeyAlgorithm;
    //   112: invokestatic isRSA : (Lio/fusionauth/domain/Key$KeyAlgorithm;)Z
    //   115: ifeq -> 206
    //   118: aload_1
    //   119: getfield length : Ljava/lang/Integer;
    //   122: invokevirtual intValue : ()I
    //   125: lookupswitch default -> 178, 2048 -> 160, 3072 -> 166, 4096 -> 172
    //   160: invokestatic generate2048_RSAKeyPair : ()Lio/fusionauth/jwt/domain/KeyPair;
    //   163: goto -> 195
    //   166: invokestatic generate3072_RSAKeyPair : ()Lio/fusionauth/jwt/domain/KeyPair;
    //   169: goto -> 195
    //   172: invokestatic generate4096_RSAKeyPair : ()Lio/fusionauth/jwt/domain/KeyPair;
    //   175: goto -> 195
    //   178: new java/lang/IllegalStateException
    //   181: dup
    //   182: aload_1
    //   183: getfield length : Ljava/lang/Integer;
    //   186: <illegal opcode> makeConcatWithConstants : (Ljava/lang/Integer;)Ljava/lang/String;
    //   191: invokespecial <init> : (Ljava/lang/String;)V
    //   194: athrow
    //   195: astore_2
    //   196: aload_1
    //   197: getstatic io/fusionauth/domain/Key$KeyType.RSA : Lio/fusionauth/domain/Key$KeyType;
    //   200: putfield type : Lio/fusionauth/domain/Key$KeyType;
    //   203: goto -> 322
    //   206: aload_1
    //   207: getfield algorithm : Lio/fusionauth/domain/Key$KeyAlgorithm;
    //   210: invokestatic isECDSA : (Lio/fusionauth/domain/Key$KeyAlgorithm;)Z
    //   213: ifeq -> 301
    //   216: getstatic io/fusionauth/api/service/system/DefaultKeyService$1.$SwitchMap$io$fusionauth$domain$Key$KeyAlgorithm : [I
    //   219: aload_1
    //   220: getfield algorithm : Lio/fusionauth/domain/Key$KeyAlgorithm;
    //   223: invokevirtual ordinal : ()I
    //   226: iaload
    //   227: tableswitch default -> 270, 4 -> 252, 5 -> 258, 6 -> 264
    //   252: invokestatic generate256_ECKeyPair : ()Lio/fusionauth/jwt/domain/KeyPair;
    //   255: goto -> 290
    //   258: invokestatic generate384_ECKeyPair : ()Lio/fusionauth/jwt/domain/KeyPair;
    //   261: goto -> 290
    //   264: invokestatic generate521_ECKeyPair : ()Lio/fusionauth/jwt/domain/KeyPair;
    //   267: goto -> 290
    //   270: new java/lang/IllegalStateException
    //   273: dup
    //   274: aload_1
    //   275: getfield algorithm : Lio/fusionauth/domain/Key$KeyAlgorithm;
    //   278: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   281: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;)Ljava/lang/String;
    //   286: invokespecial <init> : (Ljava/lang/String;)V
    //   289: athrow
    //   290: astore_2
    //   291: aload_1
    //   292: getstatic io/fusionauth/domain/Key$KeyType.EC : Lio/fusionauth/domain/Key$KeyType;
    //   295: putfield type : Lio/fusionauth/domain/Key$KeyType;
    //   298: goto -> 322
    //   301: aload_1
    //   302: getfield algorithm : Lio/fusionauth/domain/Key$KeyAlgorithm;
    //   305: invokestatic isEdDSA : (Lio/fusionauth/domain/Key$KeyAlgorithm;)Z
    //   308: ifeq -> 322
    //   311: invokestatic generate_ed25519_EdDSAKeyPair : ()Lio/fusionauth/jwt/domain/KeyPair;
    //   314: astore_2
    //   315: aload_1
    //   316: getstatic io/fusionauth/domain/Key$KeyType.OKP : Lio/fusionauth/domain/Key$KeyType;
    //   319: putfield type : Lio/fusionauth/domain/Key$KeyType;
    //   322: aload_2
    //   323: ifnull -> 342
    //   326: aload_1
    //   327: aload_2
    //   328: getfield privateKey : Ljava/lang/String;
    //   331: putfield privateKey : Ljava/lang/String;
    //   334: aload_1
    //   335: aload_2
    //   336: getfield publicKey : Ljava/lang/String;
    //   339: putfield publicKey : Ljava/lang/String;
    //   342: return
    // Line number table:
    //   Java source line number -> byte code offset
    //   #557	-> 0
    //   #558	-> 2
    //   #559	-> 12
    //   #560	-> 48
    //   #561	-> 58
    //   #562	-> 68
    //   #564	-> 78
    //   #566	-> 98
    //   #567	-> 108
    //   #568	-> 118
    //   #569	-> 160
    //   #570	-> 166
    //   #571	-> 172
    //   #573	-> 178
    //   #574	-> 195
    //   #575	-> 196
    //   #576	-> 206
    //   #577	-> 216
    //   #578	-> 252
    //   #579	-> 258
    //   #580	-> 264
    //   #582	-> 270
    //   #577	-> 290
    //   #584	-> 291
    //   #585	-> 301
    //   #586	-> 311
    //   #587	-> 315
    //   #590	-> 322
    //   #591	-> 326
    //   #592	-> 334
    //   #594	-> 342
  }
  
  private boolean privateKeyPEMish(Key paramKey) {
    if (SecurityTools.isPEMEncoded(paramKey.privateKey))
      return true; 
    return retryAsPrivatePKCS1(paramKey, paramKey -> Boolean.valueOf(retryAsPrivatePKCS8(paramKey, this::retryAsPrivateEC)));
  }
  
  private boolean publicKeyIsPEMish(Key paramKey) {
    if (SecurityTools.isPEMEncoded(paramKey.publicKey))
      return true; 
    return retryAsX509PEM(paramKey, this::retryAsPublicPKCS1);
  }
  
  private boolean retryAsCertificatePEM(Key paramKey) {
    try {
      String str = "-----BEGIN CERTIFICATE-----\n" + chopIt(paramKey.certificate) + "\n-----END CERTIFICATE-----";
      if (SecurityTools.isPEMEncoded(str)) {
        paramKey.certificate = str;
        return true;
      } 
    } catch (Exception exception) {}
    return false;
  }
  
  private boolean retryAsPrivateEC(Key paramKey) {
    try {
      String str = "-----BEGIN EC PRIVATE KEY-----\n" + chopIt(paramKey.privateKey) + "\n-----END EC PRIVATE KEY-----";
      if (SecurityTools.isPEMEncoded(str)) {
        paramKey.privateKey = str;
        return true;
      } 
    } catch (Exception exception) {}
    return false;
  }
  
  private boolean retryAsPrivatePKCS1(Key paramKey, Function<Key, Boolean> paramFunction) {
    try {
      String str = "-----BEGIN RSA PRIVATE KEY-----\n" + chopIt(paramKey.privateKey) + "\n-----END RSA PRIVATE KEY-----";
      if (SecurityTools.isPEMEncoded(str)) {
        paramKey.privateKey = str;
        return true;
      } 
    } catch (Exception exception) {}
    return (paramFunction != null && ((Boolean)paramFunction.apply(paramKey)).booleanValue());
  }
  
  private boolean retryAsPrivatePKCS8(Key paramKey, Function<Key, Boolean> paramFunction) {
    try {
      String str = "-----BEGIN PRIVATE KEY-----\n" + chopIt(paramKey.privateKey) + "\n-----END PRIVATE KEY-----";
      if (SecurityTools.isPEMEncoded(str)) {
        paramKey.privateKey = str;
        return true;
      } 
    } catch (Exception exception) {}
    return (paramFunction != null && ((Boolean)paramFunction.apply(paramKey)).booleanValue());
  }
  
  private boolean retryAsPublicPKCS1(Key paramKey) {
    try {
      String str = "-----BEGIN RSA PUBLIC KEY-----\n" + chopIt(paramKey.publicKey) + "\n-----END RSA PUBLIC KEY-----";
      if (SecurityTools.isPEMEncoded(str)) {
        paramKey.publicKey = str;
        return true;
      } 
    } catch (Exception exception) {}
    return false;
  }
  
  private boolean retryAsX509PEM(Key paramKey, Function<Key, Boolean> paramFunction) {
    try {
      String str = "-----BEGIN PUBLIC KEY-----\n" + chopIt(paramKey.publicKey) + "\n-----END PUBLIC KEY-----";
      if (SecurityTools.isPEMEncoded(str)) {
        paramKey.publicKey = str;
        return true;
      } 
    } catch (Exception exception) {}
    return (paramFunction != null && ((Boolean)paramFunction.apply(paramKey)).booleanValue());
  }
  
  private boolean x5tKidIsUnique(Key paramKey) {
    if (paramKey.kid != null)
      return true; 
    try {
      KeyHelper.setupSyntheticFields(paramKey);
      if (paramKey.certificate == null)
        return true; 
      PEM pEM = PEM.decode(paramKey.certificate);
      paramKey.kid = JWTUtils.generateJWS_x5t(pEM.certificate.getEncoded());
      return (this.keyMapper.retrieveByKid(paramKey.kid) == null);
    } catch (Exception exception) {
      return true;
    } 
  }
}
