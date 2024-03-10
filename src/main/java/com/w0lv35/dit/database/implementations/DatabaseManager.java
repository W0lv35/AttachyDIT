package com.w0lv35.dit.database.implementations;

import com.w0lv35.dit.configurator.Configurator;
import com.w0lv35.dit.database.implementations.persistence.PersistenceUnitInfo;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import com.w0lv35.dit.spigot.Plugin;
import org.hibernate.jpa.HibernatePersistenceProvider;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager implements com.w0lv35.dit.database.DatabaseManager {
    private static final String DB_URI_KEY = "db_uri";
    private static final String DB_USER_KEY = "db_user";
    private static final String DB_PASS_KEY = "db_pass";
    private HikariDataSource dataSource;
    private final Map<String, String> hibernateProperties = new HashMap<>();
    private final List<String> registeredEntityClasses;
    private final Plugin plugin;
    private EntityManagerFactory entityManagerFactory;
    private Configurator configurator;

    public DatabaseManager(List<String> registeredEntityClasses, Logger logger) {
        this.registeredEntityClasses = registeredEntityClasses;
        this.plugin = new com.w0lv35.dit.spigot.implementations.Plugin("dit", logger);
        if (!setConfig()) {
            plugin.getLogger().log(Level.SEVERE, "DIT: Cannot load values from DIT config file - cannot connect to DB without credentials");
            return;
        }
        hibernateProperties.put("hibernate.show_sql", "true");
        hibernateProperties.put("hibernate.hbm2ddl.auto","update");
        hibernateProperties.put("hibernate.physical_naming_strategy","com.w0lv35.dit.database.implementations.persistence.PluginPrefixNamingStrategy");
        hibernateProperties.put("hibernate.jdbc.batch_size", "50");
        hibernateProperties.put("hibernate.order_inserts", "true");
        hibernateProperties.put("hibernate.order_updates", "true");
        setDataSource();
        setEntityManagerFactory();
    }
    @Override
    public <T> Optional<List<T>> getPluginData(String query, Class<T> entityClass) {
        return Optional.ofNullable(withTransactionResult(entityManager -> entityManager.createQuery(query, entityClass).getResultList()));
    }
    @Override
    public <T, R> Optional<T> getPluginData(Class<T> data, R id) {
        return Optional.ofNullable(withTransactionResult(entityManager -> entityManager.find(data, id)));
    }
    @Override
    public <T> void persistPluginData(List<T> pluginData) {
        withTransaction(entityManager -> {
            for (T data : pluginData) {
                entityManager.persist(data);
            }
        });
    }
    @Override
    public <T> void removePluginData(List<T> pluginData) {
        withTransaction(entityManager -> {
            for (T data : pluginData) {
                entityManager.remove(entityManager.merge(data));
            }
        });
    }

    private <T> T withTransactionResult(Function<EntityManager, T> action) {
        if (entityManagerFactory == null) return null;

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            T result = action.apply(entityManager);
            entityManager.getTransaction().commit();
            return result;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }
    private void withTransaction(Consumer<EntityManager> action) {
        if (entityManagerFactory == null) return;

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            action.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }

    //Return false if db_uri, db_user or db_pass values are not found
    private boolean setConfig() {
        Map<String, String> requiredConfig = new HashMap<>();
        requiredConfig.put(DB_URI_KEY, "");
        requiredConfig.put(DB_USER_KEY, "");
        requiredConfig.put(DB_PASS_KEY, "");
        configurator = new com.w0lv35.dit.configurator.implementations.Configurator(requiredConfig, plugin);
        return (configurator.getRequireConfig().containsKey(DB_URI_KEY) &&
                !configurator.getRequireConfig().get(DB_URI_KEY).isEmpty() &&
                configurator.getRequireConfig().containsKey(DB_USER_KEY) &&
                !configurator.getRequireConfig().get(DB_USER_KEY).isEmpty() &&
                configurator.getRequireConfig().containsKey(DB_PASS_KEY) &&
                !configurator.getRequireConfig().get(DB_PASS_KEY).isEmpty()
        );
    }
    private void setDataSource() {
        this.dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(configurator.getRequireConfig().get(DB_URI_KEY));
        dataSource.setUsername(configurator.getRequireConfig().get(DB_USER_KEY));
        dataSource.setPassword(configurator.getRequireConfig().get(DB_PASS_KEY));
    }

    private void setEntityManagerFactory() {
        PersistenceUnitInfo persistenceUnitInfo = new PersistenceUnitInfo(
                plugin.getName(),
                dataSource,
                registeredEntityClasses
        );

        entityManagerFactory = new HibernatePersistenceProvider()
                .createContainerEntityManagerFactory(persistenceUnitInfo, hibernateProperties);

        if (entityManagerFactory == null) throw new NullPointerException("EntityManagerFactory is null");
    }
}
