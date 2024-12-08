package com.patrykdziurkowski.microserviceschat.presentation.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
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
class KickMembersTests extends ComposeContainersBase {
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
        driver.findElement(By.id("usernameInput")).sendKeys("validUser6");
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
        driver.findElement(By.id("usernameInput")).sendKeys("validUser6");
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
        driver.findElement(By.id("createChatButton")).click();
        WebElement serverContainer = driver.findElement(By.id("serverContainer"));
        assertTrue(waitForChildren(1, serverContainer));
    }

    @Test
    @Order(5)
    void logging_toADifferentUser_shouldWork() {
        driver.findElement(By.id("logoutSubmit")).click();
        driver.navigate().to("https://localhost/register");
        driver.findElement(By.id("usernameInput")).sendKeys("validUser6x1");
        driver.findElement(By.id("passwordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("confirmPasswordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("registerSubmit")).click();
        wait.until(ExpectedConditions.urlToBe("https://localhost/login"));

        driver.findElement(By.id("usernameInput")).sendKeys("validUser6x1");
        driver.findElement(By.id("passwordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("loginSubmit")).click();
        wait.until(ExpectedConditions.urlToBe("https://localhost/chats"));

        assertEquals("https://localhost/chats", driver.getCurrentUrl());
    }

    @Test
    @Order(6)
    void joiningChat_shouldIncreaseMemberCount() {
        WebElement serverContainer = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("serverContainer")));
        WebElement joinButton = wait
                .until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                        By.id("serverContainer"),
                        By.xpath(".//*[contains(@class, 'modify-membership-button')]")))
                .get(0);

        joinButton.click();
        WebElement joinConfirmButton = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.id("joinChatConfirm")));
        wait.until(ExpectedConditions.visibilityOf(joinConfirmButton));
        joinConfirmButton.click();

        boolean memberCountUpdated = wait.until(d -> {
            WebElement memberCount = serverContainer.findElement(
                    By.xpath(".//section//p[contains(text(),'members')]"));
            return memberCount.getText().startsWith("2 members");
        });
        assertTrue(memberCountUpdated);
    }

    @Test
    @Order(7)
    void logging_shouldLogIntoDiffrentUser() {
        driver.findElement(By.id("logoutSubmit")).click();
        driver.navigate().to("https://localhost/login");

        driver.findElement(By.id("usernameInput")).sendKeys("validUser6");
        driver.findElement(By.id("passwordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("loginSubmit")).click();
        wait.until(ExpectedConditions.urlToBe("https://localhost/chats"));

        assertEquals("https://localhost/chats", driver.getCurrentUrl());
    }

    @Test
    @Order(8)
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
    @Order(9)
    void loadMembersList_shouldLoad2Members_whenLoadedChat() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("memberContainer")));

        WebElement memberContainer = driver.findElement(By.id("memberContainer"));
        assertTrue(waitForChildren(2, memberContainer));
    }

    @Test
    @Order(10)
    void kickMember_shouldKick2ndMember_whenClickedButton() {
        WebElement memberContainer = driver.findElement(By.id("memberContainer"));
        WebElement memberCard = memberContainer.findElements(By.xpath("./*")).get(1);

        WebElement kickButton = memberCard.findElement(By.id("kickButton"));
        kickButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("kickMember")));
        driver.findElement(By.id("confirmKick")).click();

        assertTrue(waitForChildren(1, memberContainer));
    }

    @Test
    @Order(11)
    void leaveChat_shouldLeaveAndDeleteChat_whenNoMembersLeft() {
        WebElement leaveButton = driver.findElement(By.id("serverContainer"))
                .findElement(By.xpath(".//*[contains(@class, 'modify-membership-button')]"));

        leaveButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("leaveChat")));
        driver.findElement(By.id("leaveChatConfirm")).click();

        assertTrue(serverCardCount(0));
    }

    @AfterAll
    static void teardown() {
        driver.quit();
    }

    private boolean serverCardCount(int expectedNumberOfCards) {
        wait.until(d -> {
            WebElement serverContainer = driver.findElement(By.id("serverContainer"));
            return serverContainer.findElements(By.xpath("./*")).size() == expectedNumberOfCards;
        });
        return true;
    }

    private boolean waitForChildren(int numberOfChildren, WebElement container) {
        wait.until(d -> {
            return container.findElements(By.xpath("./*")).size() == numberOfChildren;
        });
        return true;
    }
}