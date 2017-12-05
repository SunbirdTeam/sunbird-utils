/**
 * 
 */
package org.sunbird.common.models.util.datasecurity.impl;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.sunbird.common.models.util.ConfigUtil;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.models.util.datasecurity.DecryptionService;

/**
 * @author Manzarul
 *
 */
public class DefaultDecryptionServiceImpl implements DecryptionService {
  private static String sunbird_encryption = "";

  private String sunbirdEncryption = "";

  public DefaultDecryptionServiceImpl() {
    sunbirdEncryption = ConfigUtil.getString(JsonKey.SUNBIRD_ENCRYPTION);
  }

  @Override
  public Map<String, Object> decryptData(Map<String, Object> data) {
    if (JsonKey.ON.equalsIgnoreCase(sunbirdEncryption)) {
      if (data == null) {
        return data;
      }
      Iterator<Entry<String, Object>> itr = data.entrySet().iterator();
      while (itr.hasNext()) {
        Entry<String, Object> entry = itr.next();
        if (entry.getValue() instanceof Map || entry.getValue() instanceof List) {
          // Do Nothing
        } else {
          if (null != entry.getValue()) {
            data.put(entry.getKey(), decrypt(entry.getValue() + ""));
          }
        }
      }
    }
    return data;
  }

  @Override
  public List<Map<String, Object>> decryptData(List<Map<String, Object>> data) {
    if (JsonKey.ON.equalsIgnoreCase(sunbirdEncryption)) {
      if (data == null || data.isEmpty()) {
        return data;
      }

      for (Map<String, Object> map : data) {
        decryptData(map);
      }
    }
    return data;
  }

  @Override
  public String decryptData(String data) {
    if (JsonKey.ON.equalsIgnoreCase(sunbirdEncryption)) {
      if (ProjectUtil.isStringNullOREmpty(data)) {
        return data;
      }
      if (null != data) {
        return decrypt(data);
      } else {
        return data;
      }
    } else {
      return data;
    }
  }

  /**
   * this method is used to decrypt password.
   * 
   * @param value encrypted password.
   * @return decrypted password.
   */
  public static String decrypt(String value) {
    try {
      sunbird_encryption = DefaultEncryptionServivceImpl.getSalt();
      Key key = generateKey();
      Cipher c = Cipher.getInstance(ALGORITHM);
      c.init(Cipher.DECRYPT_MODE, key);

      String dValue = null;
      String valueToDecrypt = value.trim();
      for (int i = 0; i < ITERATIONS; i++) {
        byte[] decordedValue = new sun.misc.BASE64Decoder().decodeBuffer(valueToDecrypt);
        byte[] decValue = c.doFinal(decordedValue);
        dValue = new String(decValue).substring(sunbird_encryption.length());
        valueToDecrypt = dValue;
      }
      return dValue;
    } catch (Exception ex) {
      ProjectLogger.log("Exception Occurred while decrypting value");
    }
    return value;
  }

  private static Key generateKey(){
    return new SecretKeySpec(keyValue, ALGORITHM);
  }
}
