package com.patrykdziurkowski.microserviceschat.presentation;

import java.io.File;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class ComposeContainersBase {
    protected static final String TEST_DB_PASSWORD = "exampleP@ssword123";
    @SuppressWarnings("resource")
    protected static DockerComposeContainer<?> containers = new DockerComposeContainer<>(
            new File("../../docker-compose.yaml"))
            .withExposedService("auth", 8081)
            .waitingFor("chat", Wait.forHealthcheck())
            .withEnv("MSSQL_SA_PASSWORD", TEST_DB_PASSWORD)
            .withEnv("JWT_SECRET", "8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz")
            .withBuild(true);

    static {
        containers.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> containers.stop()));
    }
}
