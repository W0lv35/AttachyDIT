package com.w0lv35.dit.configurator.implementations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.w0lv35.dit.spigot.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Configurator implements com.w0lv35.dit.configurator.Configurator {
    private static final String CONFIG_FILE_SUFFIX = ".json";
    private static final String CONFIG_DIRECTORY = "config";
    private static final String REQUIRE_KEY = "required";
    private static final String OPTION_KEY = "optional";
    private Plugin plugin;
    private File configFile;
    private Map<String, Map<String,String>> config = new HashMap<>();

    public Configurator(Plugin plugin) {
        this.config.put(REQUIRE_KEY, new HashMap<>());
        this.config.put(OPTION_KEY, new HashMap<>());
        initialize(plugin);
    }
    public Configurator(Map<String, String> requireConfig, Plugin plugin) {
        this.config.put(REQUIRE_KEY, requireConfig);
        this.config.put(OPTION_KEY, new HashMap<>());
        initialize(plugin);
    }

    @Override
    public Map<String, String> getRequireConfig() {
        return config.get(REQUIRE_KEY);
    }

    @Override
    public Map<String, String> getOptionConfig() {
        return config.get(OPTION_KEY);
    }

    @Override
    public void saveConfig() {
        if (configFile == null) return;

        try (FileWriter fileWriter = new FileWriter(configFile.getPath())) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            String json = gsonBuilder.create().toJson(config);
            fileWriter.write(json);
        } catch (IOException e) {
            plugin.getLogger().severe("DIT: Could not save config file for plugin: " + plugin.getName());
        }
        plugin.getLogger().info("DIT: Saved config file " + configFile.getPath());
    }

    @Override
    public void refreshConfig() {
        if (configFile == null) return;

        Type type = new TypeToken<Map<String,Map<String,String>>>(){}.getType();
        config.clear();
        try (FileReader fileReader = new FileReader(configFile.getPath())) {
            config = new Gson().fromJson(fileReader, type);
        } catch (IOException e) {
            plugin.getLogger().severe("DIT: Could not read config file for plugin: " + plugin.getName());
            return;
        }
        plugin.getLogger().info("DIT: Loaded config file for plugin: " + plugin.getName());
    }

    private void initialize(Plugin plugin) {
        this.plugin = plugin;
        setConfigFile();
        buildConfigDirectory();
        buildConfigFile();
        refreshConfig();
    }

    private void buildConfigDirectory() {
        if (configFile == null) return;
        File configDirectory = configFile.getParentFile();
        if (!configDirectory.exists()) {
            if (!configDirectory.mkdirs()) {
                plugin.getLogger().severe("DIT: Could not create config directory for plugin: " + plugin.getName());
            } else {
                plugin.getLogger().warning("DIT: Created config directory for plugin: " + plugin.getName());
            }
        }
    }

    private void buildConfigFile() {
        if (configFile == null || configFile.exists()) return;

        try (FileWriter fileWriter = new FileWriter(configFile.getPath())) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            String json = gsonBuilder.create().toJson(config);
            fileWriter.write(json);
        } catch (IOException e) {
            plugin.getLogger().severe("DIT: Could not create config file for plugin: " + plugin.getName());
            return;
        }
        plugin.getLogger().warning("DIT: Created default config file for plugin: " + plugin.getName());
    }

    private void setConfigFile() {
        String configPath = Util.getJarPath(plugin);
        if (configPath.isEmpty()) {
            plugin.getLogger().severe("DIT: Could locate JAR path for plugin: " + plugin.getName());
            return;
        }
        configFile = new File(configPath + "/" + CONFIG_DIRECTORY + "/" + plugin.getName() + CONFIG_FILE_SUFFIX);
    }
}
