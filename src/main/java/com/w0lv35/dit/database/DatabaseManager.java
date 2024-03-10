package com.w0lv35.dit.database;

import java.util.List;
import java.util.Optional;

public interface DatabaseManager {

    <T> Optional<List<T>> getPluginData(String query, Class<T> entityClass);

    <T, R> Optional<T> getPluginData(Class<T> entityClass, R id);

    <T> void persistPluginData(List<T> pluginData);

    <T> void removePluginData(List<T> pluginData);
}
