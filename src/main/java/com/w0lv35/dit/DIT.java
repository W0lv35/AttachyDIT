package com.w0lv35.dit;

import com.w0lv35.dit.database.implementations.persistence.PluginPrefixNamingStrategy;
import com.w0lv35.dit.spigot.implementations.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class DIT {
    private final Plugin plugin;
    private final Manager manager;
    public DIT(JavaPlugin plugin) {
        this.plugin = new Plugin(plugin.getName(), plugin.getLogger());
        PluginPrefixNamingStrategy.setPrefix(plugin.getName());
        manager = new Manager(this.plugin);
    }
    public Manager getManager() {
        return manager;
    }
}
