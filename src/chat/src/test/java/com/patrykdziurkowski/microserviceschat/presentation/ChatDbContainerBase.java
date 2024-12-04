package com.patrykdziurkowski.microserviceschat.presentation;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

public class ChatDbContainerBase {
    @SuppressWarnings("resource")
    @Container
    @ServiceConnection
    protected static MSSQLServerContainer<?> db = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-latest")
            .withExposedPorts(1433)
            .waitingFor(Wait.forSuccessfulCommand(
                    "/opt/mssql-tools18/bin/sqlcmd -U sa -S localhost -P examplePassword123 -No -Q 'SELECT 1'"))
            .acceptLicense()
            .withPassword("P@ssw0rd");

    static {
        db.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> db.stop()));
    }
}
