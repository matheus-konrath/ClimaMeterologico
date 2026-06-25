package com.painelmeteorologico.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = ConnectionFactory.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException("db.properties não encontrado em src/main/resources");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar db.properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        );
    }
}
