/**
 * 
 */
package org.sunbird.common.models.util.fcm;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.sunbird.common.models.util.ConfigUtil;
import org.sunbird.common.models.util.HttpUtil;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.ProjectUtil;

/**
 * @author Manzarul
 *
 */
public class Notification {
  /**
   * FCM_URL URL of FCM server
   */
  public static final String FCM_URL = ConfigUtil.getString(JsonKey.FCM_URL);
  /**
   * FCM_ACCOUNT_KEY FCM server key.
   */
  private static final String FCM_ACCOUNT_KEY = ConfigUtil.getString(JsonKey.SUNBIRD_FCM_ACCOUNT_KEY);
  private static Map<String,String> headerMap = new HashMap<>();
  private static final String TOPIC_SUFFIX = "/topics/"; 
  static{
     headerMap.put(JsonKey.AUTHORIZATION, FCM_ACCOUNT_KEY);
     headerMap.put("Content-Type", "application/json");
  }

  /**
   * This method will send notification to FCM.
   * @param topic String
   * @param data Map<String, Object>
   * @param url String
   * @return String  as Json.{"message_id": 7253391319867149192}
   */
  public static String sendNotification(String topic, Map<String, Object> data,
      String url) {
    if (ProjectUtil.isStringNullOREmpty(FCM_ACCOUNT_KEY) || ProjectUtil.isStringNullOREmpty(url)) {
      ProjectLogger.log("FCM account key or URL is not provided===" + FCM_URL,
          LoggerEnum.INFO.name());
      return JsonKey.FAILURE;
    }
    String response = null;
    try {
      JSONObject object1 = new JSONObject(data);
      JSONObject object = new JSONObject();
      object.put(JsonKey.DATA, object1);
      object.put(JsonKey.TO, TOPIC_SUFFIX+topic);
      response =
          HttpUtil.sendPostRequest(FCM_URL, object.toString(), headerMap);
      ProjectLogger.log("FCM Notification response== for topic "+ topic  + response);
      object1 = null;
      object1 = new JSONObject(response);
      long val = object1.getLong(JsonKey.MESSAGE_Id);
      response = val+"";
    } catch (Exception e) {
      response =  JsonKey.FAILURE;
      ProjectLogger.log(e.getMessage(), e);
    }
    return response;
  }
}
