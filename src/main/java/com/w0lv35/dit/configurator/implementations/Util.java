package com.w0lv35.dit.configurator.implementations;

import com.w0lv35.dit.spigot.Plugin;

import java.io.File;
class Util {
        public static String getJarPath(Plugin plugin)  {
            try {
                String jarPath = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                return new File(jarPath).getParent();
            } catch (Exception e) {
                return "";
            }
        }
}
