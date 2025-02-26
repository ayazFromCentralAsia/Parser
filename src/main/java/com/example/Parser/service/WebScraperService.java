package com.example.Parser.service;

import com.example.Parser.dto.ScarpedDataResponse;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
public class WebScraperService {



    public ScarpedDataResponse scrapeWildberriesProduct(String url) throws Exception {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        GenericContainer seleniumContainer = new GenericContainer("selenium/standalone-chrome:latest")
                .withExposedPorts(4444)
                .waitingFor(Wait.forListeningPort());
        seleniumContainer.start();

        String seleniumHost = seleniumContainer.getContainerIpAddress();
        Integer seleniumPort = seleniumContainer.getMappedPort(4444);

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new RemoteWebDriver(
                new URL("http://" + seleniumHost + ":" + seleniumPort + "/wd/hub"), options);

        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            ScarpedDataResponse scarpedDataResponse = new ScarpedDataResponse();
            scarpedDataResponse.setUrl(url);

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(@class, 'product-page__title')]")));
                String productTitle = driver.findElement(By.xpath("//h1[contains(@class, 'product-page__title')]")).getText();
                scarpedDataResponse.setTitle(productTitle);
            } catch (Exception e) {
                scarpedDataResponse.setTitle(driver.getTitle());
            }

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[contains(@class, 'price-block__final-price')]")));
                String price = driver.findElement(By.xpath("//span[contains(@class, 'price-block__final-price')]")).getText();
                scarpedDataResponse.setDescription("Цена: " + price);
            } catch (Exception e) {
                scarpedDataResponse.setDescription("");
            }

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p[contains(@class, 'collapsable__text')]")));
                String description = driver.findElement(By.xpath("//p[contains(@class, 'collapsable__text')]")).getText();

                String currentDesc = scarpedDataResponse.getDescription();
                scarpedDataResponse.setDescription((currentDesc != null && !currentDesc.isEmpty() ? currentDesc + "\n\n" : "") + "Описание: " + description);
            } catch (Exception e) {
            }

            // Извлечение бренда
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@class, 'brand-logo__link')]")));
                String brand = driver.findElement(By.xpath("//a[contains(@class, 'brand-logo__link')]")).getAttribute("title");
                scarpedDataResponse.setCategory(brand);
            } catch (Exception e) {
                scarpedDataResponse.setCategory("");
            }

            try {
                StringBuilder content = new StringBuilder();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'product-params')]")));
                List<WebElement> paramRows = driver.findElements(By.xpath("//div[contains(@class, 'product-params')]//tr"));

                for (WebElement row : paramRows) {
                    String paramName = row.findElement(By.xpath(".//th")).getText();
                    String paramValue = row.findElement(By.xpath(".//td")).getText();
                    content.append(paramName).append(": ").append(paramValue).append("\n");
                }

                scarpedDataResponse.setContent(content.toString());
            } catch (Exception e) {
                scarpedDataResponse.setContent("");
            }

            scarpedDataResponse.setLanguage("ru");
            scarpedDataResponse.setLicense("");

            return scarpedDataResponse;
        } finally {
            if (driver != null) {
                driver.quit();
            }
            seleniumContainer.stop();
        }
    }
}
