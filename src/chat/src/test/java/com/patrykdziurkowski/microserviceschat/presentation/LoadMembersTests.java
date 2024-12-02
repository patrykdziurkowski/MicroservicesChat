package com.patrykdziurkowski.microserviceschat.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(properties = {
        // disable the chat database run locally for these tests since we're running it
        // inside a container
        "spring.jpa.hibernate.ddl-auto=none"
})
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
class LoadMembersTests extends ComposeContainersBase {
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
    void website_shouldLoad() {
        driver.navigate().to("https://localhost");

        assertTrue(driver.getPageSource().contains("Let's chat!"));
    }

    @Test
    @Order(2)
    void registeringUser_shouldRedirect_toLogin() {
        driver.navigate().to("https://localhost/register");
        driver.findElement(By.id("usernameInput")).sendKeys("validUser5");
        driver.findElement(By.id("passwordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("confirmPasswordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("registerSubmit")).click();

        wait.until(ExpectedConditions.urlToBe("https://localhost/login"));
        assertEquals("https://localhost/login", driver.getCurrentUrl());
    }

    @Test
    @Order(3)
    void loggingIn_shouldRedirect_toChats() {
        driver.navigate().to("https://localhost/login");
        driver.findElement(By.id("usernameInput")).sendKeys("validUser5");
        driver.findElement(By.id("passwordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("loginSubmit")).click();

        wait.until(ExpectedConditions.urlToBe("https://localhost/chats"));
        assertEquals("https://localhost/chats", driver.getCurrentUrl());
    }

    @Test
    @Order(4)
    void loadingChats_shouldLoadNewlyCreatedChat() {
        driver.navigate().to("https://localhost/chats");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("serverContainer")));

        driver.findElement(By.id("showCreateChatModal")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("createChat")));
        driver.findElement(By.id("createChatName")).sendKeys("ChatRoom");
        driver.findElement(By.id("createChatIsPrivate")).click();
        driver.findElement(By.id("createChatButton")).click();
        WebElement serverContainer = driver.findElement(By.id("serverContainer"));
        assertTrue(waitForChildren(1, serverContainer));
    }

    @Test
    @Order(4)
    void loadingChat_shouldRedirect_whenClickedChat() {
        driver.navigate().to("https://localhost/chats");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("serverContainer")));
        WebElement serverContainer = driver.findElement(By.id("serverContainer"));
        waitForChildren(1, serverContainer);

        WebElement chatCard = serverContainer.findElements(By.xpath("./*")).get(0);
        chatCard.click();
        wait.until(ExpectedConditions.urlContains("https://localhost/chats/"));
        assertTrue(true);
    }

    @Test
    @Order(5)
    void loadMembersList_shouldLoadMember_whenLoadedChat() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("memberContainer")));

        WebElement memberContainer = driver.findElement(By.id("memberContainer"));
        assertTrue(waitForChildren(1, memberContainer));
    }

    @AfterAll
    static void teardown() {
        driver.quit();
    }

    private boolean waitForChildren(int numberOfChildren, WebElement container) {
        wait.until(d -> {
            return container.findElements(By.xpath("./*")).size() == numberOfChildren;
        });
        return true;
    }
}