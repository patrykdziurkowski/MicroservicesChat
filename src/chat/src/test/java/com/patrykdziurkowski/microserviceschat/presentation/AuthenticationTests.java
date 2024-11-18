package com.patrykdziurkowski.microserviceschat.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        // disable the chat database run locally for these tests since we're running it
        // inside a container
        "spring.jpa.hibernate.ddl-auto=none"
})
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
class AuthenticationTests {
    private static WebDriver driver;
    @SuppressWarnings("resource")
    @Container
    private static DockerComposeContainer<?> containers = new DockerComposeContainer<>(
            new File("../../docker-compose.yaml"))
            .waitingFor("chat", Wait.forHealthcheck())
            .withEnv("MSSQL_SA_PASSWORD", "exampleP@ssword123")
            .withEnv("JWT_SECRET", "8bRmGYY9bsVaS6G4HlIREIQqkPOTUNVRZtF6hgh+qyZitTwD/kuYOOYs7XnQ5vnz")
            .withBuild(true);

    @BeforeAll
    static void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        driver = new ChromeDriver(options);
    }

    @Test
    @Order(1)
    void website_shouldLoad() {
        driver.navigate().to("https://localhost");

        assertTrue(driver.getPageSource().contains("Let's chat!"));
    }

    @Test
    @Order(2)
    void accessingChats_shouldFail_whenNotLoggedIn() {
        driver.navigate().to("https://localhost/chats");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.urlToBe("https://localhost/login"));
        assertEquals("https://localhost/login", driver.getCurrentUrl());
    }

    @Test
    @Order(3)
    void registeringUser_shouldRedirect_toLogin() {
        driver.navigate().to("https://localhost/register");
        driver.findElement(By.id("usernameInput")).sendKeys("validUser1");
        driver.findElement(By.id("passwordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("confirmPasswordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("registerSubmit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.urlToBe("https://localhost/login"));
        assertEquals("https://localhost/login", driver.getCurrentUrl());
    }

    @Test
    @Order(4)
    void loggingIn_shouldRedirect_toChats() {
        driver.navigate().to("https://localhost/login");
        driver.findElement(By.id("usernameInput")).sendKeys("validUser1");
        driver.findElement(By.id("passwordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("loginSubmit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.urlToBe("https://localhost/chats"));
        assertEquals("https://localhost/chats", driver.getCurrentUrl());
    }

    @Test
    @Order(5)
    void loggingOut_shouldRedirect_toRoot() {
        driver.navigate().to("https://localhost/chats");
        driver.findElement(By.id("logoutSubmit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.urlToBe("https://localhost/"));
        assertEquals("https://localhost/", driver.getCurrentUrl());
    }

    @Test
    @Order(6)
    void accessingChats_shouldNotWork_afterLogout() {
        driver.navigate().to("https://localhost/chats");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.urlToBe("https://localhost/login"));
        assertEquals("https://localhost/login", driver.getCurrentUrl());
    }

    @AfterAll
    static void teardown() {
        driver.quit();
    }
}