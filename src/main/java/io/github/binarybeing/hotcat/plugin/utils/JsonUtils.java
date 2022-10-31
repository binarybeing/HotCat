package io.github.binarybeing.hotcat.plugin.utils;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class JsonUtils {
    public static String readJsonFileStringValue(File json, String key) {
        try {
            String s = FileUtils.readFileToString(json, "UTF-8");
            JsonElement element = JsonParser.parseString(s);
            return element.getAsJsonObject().get(key).getAsString();
        } catch (Exception e) {
            LogUtils.addLog("json parse error: " + e.getMessage());
        }
        return null;
    }
    public static JsonElement readJsonFile(File json) {
        try {
            String s = FileUtils.readFileToString(json, "UTF-8");
            return JsonParser.parseString(s);
        } catch (Exception e) {
            LogUtils.addLog("json parse error: " + e.getMessage());
        }
        return null;
    }

    public static String readJsonStringValue(JsonObject json, String key) {
        try {
            return json.get(key).getAsString();
        } catch (Exception e) {
            LogUtils.addLog("json parse error: " + e.getMessage());
        }
        return null;
    }
    public static Long readJsonLongValue(JsonObject json, String key){
        try {
            return json.get(key).getAsLong();
        } catch (Exception e) {
            LogUtils.addLog("json parse error: " + e.getMessage());
        }
        return null;
    }

    public static JsonObject readJsonObjectValue(JsonObject jsonObject, String panel) {
        try {
            return jsonObject.get(panel).getAsJsonObject();
        } catch (Exception e) {
            LogUtils.addLog("json parse error: " + e.getMessage());
        }
        return null;
    }
}
