package com.patrykdziurkowski.microserviceschat.presentation.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.regex.Pattern;

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

import com.patrykdziurkowski.microserviceschat.presentation.ChatApplication;
import com.patrykdziurkowski.microserviceschat.presentation.ComposeContainersBase;

@SpringBootTest(properties = {
        // disable the chat database run locally for these tests since we're running it
        // inside a container
        "spring.jpa.hibernate.ddl-auto=none"
})
@ContextConfiguration(classes = ChatApplication.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
class ChangeUserNameTests extends ComposeContainersBase {
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
        driver.findElement(By.id("usernameInput")).sendKeys("validUser52");
        driver.findElement(By.id("passwordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("confirmPasswordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("registerSubmit")).click();
        wait.until(ExpectedConditions.urlToBe("https://localhost/login"));

        driver.findElement(By.id("usernameInput")).sendKeys("validUser52");
        driver.findElement(By.id("passwordInput")).sendKeys("P@ssword1!");
        driver.findElement(By.id("loginSubmit")).click();
        wait.until(ExpectedConditions.urlToBe("https://localhost/chats"));

        assertEquals("https://localhost/chats", driver.getCurrentUrl());
    }

    @Test
    @Order(2)
    void userNameText_shouldBeEqualToUserName_whenPageLoaded() {
        driver.navigate().to("https://localhost/chats");
        wait.until(ExpectedConditions.urlToBe("https://localhost/chats"));
        assertEquals("validUser52", driver.findElement(By.id("userNameLabel")).getText());
    }

    @Test
    @Order(3)
    void userNameText_shouldChangeToNewUserName_whenNameChanged() {
        driver.findElement(By.id("userSettingsButton")).click();

        WebElement userNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usernameInput")));
        userNameInput.sendKeys("newValidUser52");
        driver.findElement(By.id("saveUsernameButton")).click();

        wait.until(ExpectedConditions.textMatches(By.id("userNameLabel"), Pattern.compile("newValidUser52")));
    }

    @Test
    @Order(4)
    void userNameText_shouldStayAsNewUserName_whenPageRefreshed() {
        driver.navigate().to("https://localhost/chats");
        wait.until(ExpectedConditions.urlToBe("https://localhost/chats"));
        driver.navigate().refresh();

        assertEquals("newValidUser52", driver.findElement(By.id("userNameLabel")).getText());
    }

}
