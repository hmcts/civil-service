package uk.gov.hmcts.reform.civil.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

    private JsonUtil() {
    }

    public static String getValueByKey(String jsonString, String key) {

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            return null;
        }

        Object value = getValueByKey(jsonObject, key);
        return value != null ? value.toString() : null;
    }

    private static Object getValueByKey(JSONObject jsonObject, String key) {
        if (jsonObject.has(key)) {
            return jsonObject.get(key);
        }

        for (String k : jsonObject.keySet()) {
            Object value = jsonObject.get(k);
            if (value instanceof JSONObject) {
                Object result = getValueByKey((JSONObject) value, key);
                if (result != null) {
                    return result;
                }
            } else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                for (int i = 0; i < array.length(); i++) {
                    Object arrayElement = array.get(i);
                    if (arrayElement instanceof JSONObject) {
                        Object result = getValueByKey((JSONObject) arrayElement, key);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }
}
