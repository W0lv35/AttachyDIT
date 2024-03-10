package com.w0lv35.dit.spigot.implementations;

import java.util.logging.Logger;


public class Plugin implements com.w0lv35.dit.spigot.Plugin {
    private final String name;
    private final Logger logger;

    public Plugin(String name, Logger logger) {
        this.name = name;
        this.logger = logger;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public Logger getLogger() {
        return logger;
    }
}
