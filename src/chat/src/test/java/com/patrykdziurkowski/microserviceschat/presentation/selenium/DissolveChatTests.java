package com.patrykdziurkowski.microserviceschat.presentation.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.patrykdziurkowski.microserviceschat.presentation.ComposeContainersBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DissolveChatTests extends ComposeContainersBase {
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.password", () -> TEST_DB_PASSWORD);
    }

    @Test
    @Order(1)
    void authentication_shouldSucceed() {
        driver.navigate().to("https://localhost/register");
        driver.findElement(By.id("usernameInput")).sendKeys("validUser7");
        driver.findElement(By.id("passwordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("confirmPasswordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("registerSubmit")).click();
        wait.until(ExpectedConditions.urlToBe("https://localhost/login"));
        driver.findElement(By.id("usernameInput")).sendKeys("validUser7");
        driver.findElement(By.id("passwordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("loginSubmit")).click();

        wait.until(ExpectedConditions.urlToBe("https://localhost/chats"));

        assertEquals("https://localhost/chats", driver.getCurrentUrl());
    }

    @Test
    @Order(2)
    void creatingAChat_shouldWork_whenGivenValidData() {
        driver.navigate().to("https://localhost/chats");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("serverContainer")));

        driver.findElement(By.id("showCreateChatModal")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("createChat")));
        driver.findElement(By.id("createChatName")).sendKeys("ChatRoom");
        driver.findElement(By.id("createChatIsPrivate")).click();
        driver.findElement(By.id("createChatButton")).click();
        assertTrue(serverCardCount(1));
    }

    @Test
    @Order(3)
    void dissolveChat_shouldRemoveTheChat() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("serverContainer")));
        WebElement serverContainer = driver.findElement(By.id("serverContainer"));
        waitForChildren(1, serverContainer);
        WebElement chatCard = serverContainer.findElements(By.xpath("./*")).get(0);
        chatCard.click();
        wait.until(ExpectedConditions.urlContains("https://localhost/chats/"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dissolveChatButton")));
        driver.findElement(By.id("dissolveChatButton")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dissolveChat")));
        driver.findElement(By.id("confirmDissolveChat")).click();

        assertTrue(serverCardCount(0));
    }

    private boolean waitForChildren(int numberOfChildren, WebElement container) {
        wait.until(d -> {
            return container.findElements(By.xpath("./*")).size() == numberOfChildren;
        });
        return true;
    }

    private boolean serverCardCount(int expectedNumberOfCards) {
        wait.until(d -> {
            WebElement serverContainer = driver.findElement(By.id("serverContainer"));
            return serverContainer.findElements(By.xpath("./*")).size() == expectedNumberOfCards;
        });
        return true;
    }
}
