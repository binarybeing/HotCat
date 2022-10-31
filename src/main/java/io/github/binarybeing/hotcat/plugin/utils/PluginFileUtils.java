package io.github.binarybeing.hotcat.plugin.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.util.io.ZipUtil;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gn.binarybei
 * @date 2022/9/23
 * @note
 */
public class PluginFileUtils {
    private static String CMD_HOME;

    private static final String PLUGIN_DIR_NAME = ".HotCatPlugins";

    public static String getCmdHome() {
        try {
            if (CMD_HOME == null) {
                URL url = PluginFileUtils.class.getClassLoader().getResource("META-INF/plugin.xml");

                if (null == url) {
                    return "";
                }
                String path = url.getPath();
                path = URLDecoder.decode(path, StandardCharsets.UTF_8);
                LogUtils.addLog("plugin.xml path: " + path);
                File file = new File(path.replace("file:",""));
                File parentFile = file.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
                CMD_HOME = parentFile.getAbsolutePath();
            }
            return CMD_HOME;
        } catch (Exception e) {
            LogUtils.addLog("getCmdHome error: " + e.getMessage());
            return "";
        }
    }

    public static String getPluginDirName(){
        return getCmdHome() + "/" + PLUGIN_DIR_NAME;
    }

    public static List<PluginEntity> listPlugin(){
        return doListPlugins(getPluginDirName());
    }


    private static List<PluginEntity> doListPlugins(String dir){
        File file = new File(dir);
        LogUtils.addLog("listPlugin from : " + file.getAbsolutePath());
        if (!file.exists() && !file.mkdir()) {
            LogUtils.addLog("listPlugin fail, plugin dir load fail : " + file.getAbsolutePath());
            return Collections.emptyList();
        }
        File[] files = file.listFiles();
        if(files==null){
            LogUtils.addLog("listPlugin fail, plugin dir has no file : " + file.getAbsolutePath());
            return Collections.emptyList();
        }
        LogUtils.addLog("listPlugin plugin dir : " + file.getAbsolutePath());
        List<File> pluginList = Arrays.stream(files).filter(File::isDirectory).filter(f -> {
            File[] subDirFiles = f.listFiles();
            if (subDirFiles == null) {
                return false;
            }
            return Arrays.stream(subDirFiles).anyMatch(fl -> "__main__.py".equals(fl.getName()));
        }).collect(Collectors.toList());
        if(pluginList.isEmpty()){
            LogUtils.addLog("listPlugin fail, plugin dir has no plugin : " + file.getAbsolutePath());
            return Collections.emptyList();
        }
        List<PluginEntity> pluginEntities = new ArrayList<>();
        pluginList.forEach(f -> {
            File[] subFiles = f.listFiles();
            for (File subFile : subFiles) {
                if ("plugin.json".equals(subFile.getName())) {
                    String name = JsonUtils.readJsonFileStringValue(subFile, "name");
                    if (StringUtils.isEmpty(name)) {
                        continue;
                    }
                    PluginEntity pluginEntity = new PluginEntity();
                    pluginEntity.setName(name);
                    pluginEntity.setFile(f);
                    pluginEntity.setSubMenus(doListPlugins(f.getAbsolutePath()));
                    pluginEntities.add(pluginEntity);
                }
            }
        });
        return pluginEntities;
    }
    public static void deleteDir(File dir) {
        try {
            FileUtils.forceDelete(dir);
        } catch (IOException e) {
            LogUtils.addLog("deleteDir error: " + e.getMessage());
        }
    }

    public static File unzip(File file, String pluginDirName) {
        try {
            LogUtils.addLog("unzip plugin file: " + file.getAbsolutePath());
            File pluginHome = new File(pluginDirName);
            if (!pluginHome.exists() && !pluginHome.mkdir()) {
                LogUtils.addLog("unzip plugin fail, plugin dir load fail : " + pluginHome.getAbsolutePath());
                return null;
            }
            File tempDir = new File(pluginHome, "temp");
            ZipUtil.extract(Path.of(file.toURI()), Path.of(tempDir.toURI()), null, true);
            File[] files = tempDir.listFiles();
            if (files == null || files.length == 0) {
                LogUtils.addLog("unzip plugin fail, plugin dir has no file : " + tempDir.getAbsolutePath());
                boolean delete = tempDir.delete();
                return null;
            }
            File pluginFile = files[0];
            File deployTargetFile = new File(pluginDirName + "/" + pluginFile.getName());
            if (deployTargetFile.exists()) {
                deleteDir(deployTargetFile);
            }
            boolean rename = pluginFile.renameTo(deployTargetFile);
            if (!rename) {
                LogUtils.addLog("unzip plugin fail, plugin dir rename fail : " + deployTargetFile.getAbsolutePath());
                tempDir.delete();
                return null;
            }
            if (!tempDir.delete()) {
                LogUtils.addLog("unzip plugin success: " + file.getAbsolutePath());
                return null;
            }
            LogUtils.addLog("unzip plugin success: " + file.getAbsolutePath());
            return deployTargetFile;
        } catch (Exception e) {
            LogUtils.addLog("unzip plugin fail: " + e.getMessage());
            return null;
        }
    }



}
