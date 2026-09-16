package io.fusionauth.app.action.admin.key;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.util.DownloadTools;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.http.server.HTTPResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.NoOp;

@NoOp
@Action(value = "{keyId}", requiresAuthentication = true, constraints = {"admin", "key_manager"})
public class DownloadAction extends BaseAction {
  public UUID keyId;
  
  @Inject
  public DownloadAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    Key key = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.keyId))).key;
    if (key == null)
      throw new NotFoundException(); 
    if (key.type == Key.KeyType.HMAC)
      return "success"; 
    buildZip(key);
    return "success";
  }
  
  private void addFile(ZipOutputStream paramZipOutputStream, String paramString1, String paramString2) throws IOException {
    if (paramString2 == null)
      return; 
    ZipEntry zipEntry = new ZipEntry(paramString1);
    paramZipOutputStream.putNextEntry(zipEntry);
    byte[] arrayOfByte = paramString2.getBytes(StandardCharsets.UTF_8);
    paramZipOutputStream.write(arrayOfByte, 0, arrayOfByte.length);
    paramZipOutputStream.closeEntry();
  }
  
  private void buildZip(Key paramKey) {
    try {
      HTTPResponse hTTPResponse = this.frontEndSupport.response;
      OutputStream outputStream = DownloadTools.beginDownloadResponse(hTTPResponse, "application/zip", paramKey.name + ".zip");
      ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream));
      try {
        zipOutputStream.setLevel(9);
        zipOutputStream.putNextEntry(new ZipEntry("keys/"));
        addFile(zipOutputStream, "keys/public-key.pem", paramKey.publicKey);
        addFile(zipOutputStream, "keys/public-key.pub", paramKey.publicKey.replaceAll("-----.+-----", "").replace("\n", ""));
        addFile(zipOutputStream, "keys/certificate.crt", paramKey.certificate);
        if (paramKey.certificateInformation != null)
          addFile(zipOutputStream, "keys/fingerprints.txt", "Serial number          " + paramKey.certificateInformation.serialNumber + "\n unformatted -->       " + paramKey.certificateInformation.serialNumber
              
              .replace(":", "") + "\n\nFingerprint (MD5)      " + paramKey.certificateInformation.md5Fingerprint + "\n unformatted -->       " + paramKey.certificateInformation.md5Fingerprint

              
              .replace(":", "") + "\n\nFingerprint (SHA-1)    " + paramKey.certificateInformation.sha1Fingerprint + "\n unformatted -->       " + paramKey.certificateInformation.sha1Fingerprint

              
              .replace(":", "") + "\n\nFingerprint (SHA-256)  " + paramKey.certificateInformation.sha256Fingerprint + "\n unformatted -->       " + paramKey.certificateInformation.sha256Fingerprint

              
              .replace(":", "") + "\n\nThumbprint (SHA-1)     " + paramKey.certificateInformation.sha1Thumbprint + "\n\nThumbprint (SHA-256)   " + paramKey.certificateInformation.sha256Thumbprint + "\n"); 
        zipOutputStream.close();
      } catch (Throwable throwable) {
        try {
          zipOutputStream.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (Exception exception) {
      throw new ErrorException(exception, new Object[0]);
    } 
  }
}
