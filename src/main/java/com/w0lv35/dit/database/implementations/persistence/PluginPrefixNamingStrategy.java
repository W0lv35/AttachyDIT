package com.w0lv35.dit.database.implementations.persistence;

import com.w0lv35.dit.spigot.Plugin;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;

public class PluginPrefixNamingStrategy extends PhysicalNamingStrategyStandardImpl {
    private static String m_prefix;

    public static void setPrefix(String prefix) {
        m_prefix = prefix;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return new Identifier(m_prefix + "_" + name.getText(), name.isQuoted());
    }
}
