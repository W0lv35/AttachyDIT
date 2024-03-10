package com.w0lv35.dit.configurator;

import java.util.Map;

public interface Configurator {
    public Map<String, String> getRequireConfig();
    public Map<String, String> getOptionConfig();
    public void saveConfig();
    public void refreshConfig();
}
