package com.w0lv35.dit;

import com.w0lv35.dit.configurator.Configurator;
import com.w0lv35.dit.database.DatabaseManager;
import com.w0lv35.dit.spigot.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Manager {
    private final Plugin plugin;
    private Configurator configurator;
    private DatabaseManager databaseManager;
    public Manager(Plugin plugin) {
        this.plugin = plugin;
    }
    public Configurator getConfigurator() {
        if (configurator == null) configurator = new com.w0lv35.dit.configurator.implementations.Configurator(plugin);
        return configurator;

    }
    public Configurator getConfigurator(HashMap<String, String> requireConfig) {
        if (configurator == null) configurator = new com.w0lv35.dit.configurator.implementations.Configurator(requireConfig, plugin);
        return configurator;
    }

    public DatabaseManager getDatabaseManager(List<String> registeredEntityClasses) {
        if (databaseManager == null) databaseManager = new com.w0lv35.dit.database.implementations.DatabaseManager(registeredEntityClasses, plugin.getLogger());
        return databaseManager;
    }

    public Optional<DatabaseManager> getRegisteredDatabaseManager() {
        if (databaseManager == null) return Optional.empty();
        return Optional.of(databaseManager);
    }
}
