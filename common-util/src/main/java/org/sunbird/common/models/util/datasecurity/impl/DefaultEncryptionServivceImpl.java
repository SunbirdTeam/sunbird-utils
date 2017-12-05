/**
 * 
 */
package org.sunbird.common.models.util.datasecurity.impl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.ConfigUtil;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.models.util.datasecurity.EncryptionService;
import org.sunbird.common.responsecode.ResponseCode;



/**
 * Default data encryption service
 * 
 * @author Manzarul
 *
 */
public class DefaultEncryptionServivceImpl implements EncryptionService {

  private static String encryption_key = "";

  private String sunbirdEncryption = "";

  public DefaultEncryptionServivceImpl() {
    sunbirdEncryption = ConfigUtil.getString(JsonKey.SUNBIRD_ENCRYPTION);
  }

  @Override
  public Map<String, Object> encryptData(Map<String, Object> data) throws Exception {
    if (JsonKey.ON.equalsIgnoreCase(sunbirdEncryption)) {
      if (data == null) {
        return data;
      }
      Iterator<Entry<String, Object>> itr = data.entrySet().iterator();
      while (itr.hasNext()) {
        Entry<String, Object> entry = itr.next();
        if (entry.getValue() instanceof Map || entry.getValue() instanceof List) {
          // do nothing
        } else {
          if (null != entry.getValue()) {
            data.put(entry.getKey(), encrypt(entry.getValue() + ""));
          }
        }
      }
    }
    return data;
  }

  @Override
  public List<Map<String, Object>> encryptData(List<Map<String, Object>> data) throws Exception {
    if (JsonKey.ON.equalsIgnoreCase(sunbirdEncryption)) {
      if (data == null || data.isEmpty()) {
        return data;
      }
      for (Map<String, Object> map : data) {
        encryptData(map);
      }
    }
    return data;
  }

  @Override
  public String encryptData(String data) throws Exception {
    if (JsonKey.ON.equalsIgnoreCase(sunbirdEncryption)) {
      if (ProjectUtil.isStringNullOREmpty(data)) {
        return data;
      }
      if (null != data) {
        return encrypt(data);
      } else {
        return data;
      }
    } else {
      return data;
    }
  }



  /**
   * this method is used to encrypt the password.
   * 
   * @param value String password
   * @param encryption_key
   * @return encrypted password.
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws BadPaddingException
   * @throws IllegalBlockSizeException
   */
  @SuppressWarnings("restriction")
  public static String encrypt(String value) throws NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    encryption_key = getSalt();
    Key key = generateKey();
    Cipher c = Cipher.getInstance(ALGORITHM);
    c.init(Cipher.ENCRYPT_MODE, key);

    String valueToEnc = null;
    String eValue = value;
    for (int i = 0; i < ITERATIONS; i++) {
      valueToEnc = encryption_key + eValue;
      byte[] encValue = c.doFinal(valueToEnc.getBytes());
      eValue = new sun.misc.BASE64Encoder().encode(encValue);
    }
    return eValue;
  }

  private static Key generateKey() {
    return new SecretKeySpec(keyValue, ALGORITHM);
  }


  /**
   * 
   * @return
   */
  public static String getSalt() {
    if (!ProjectUtil.isStringNullOREmpty(encryption_key)) {
      return encryption_key;
    } else {
      encryption_key = ConfigUtil.getString(JsonKey.ENCRYPTION_KEY);
    }
    if (ProjectUtil.isStringNullOREmpty(encryption_key)) {
      ProjectLogger.log("throwing exception for invalid salt==", LoggerEnum.INFO.name());
      throw new ProjectCommonException(ResponseCode.saltValue.getErrorCode(),
          ResponseCode.saltValue.getErrorMessage(), ResponseCode.SERVER_ERROR.getResponseCode());
    }
    return encryption_key;
  }

}
