package org.example.nordicnestshop.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class CustomPostgresqlContainer extends PostgreSQLContainer<CustomPostgresqlContainer> {
    private static final String DB_IMAGE = "postgres:16";

    private static CustomPostgresqlContainer postgresqlContainer;

    private CustomPostgresqlContainer() {
        super(DB_IMAGE);
    }

    public static synchronized CustomPostgresqlContainer getInstance() {
        if (postgresqlContainer == null) {
            postgresqlContainer = new CustomPostgresqlContainer();
        }
        return postgresqlContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("TEST_DB_URL", postgresqlContainer.getJdbcUrl());
        System.setProperty("TEST_DB_USERNAME", postgresqlContainer.getUsername());
        System.setProperty("TEST_DB_PASSWORD", postgresqlContainer.getPassword());
    }

    @Override
    public void stop() {
    }
}
